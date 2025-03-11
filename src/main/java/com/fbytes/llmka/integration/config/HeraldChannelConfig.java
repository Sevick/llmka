package com.fbytes.llmka.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.Mapping;
import com.fbytes.llmka.model.heraldchannel.HeraldChannel;
import com.fbytes.llmka.model.heraldchannel.HeraldChannelFactory;
import com.fbytes.llmka.model.heraldchannel.HeraldChannelTelegram;
import com.fbytes.llmka.service.ConfigReader.impl.HeraldChannelConfigReader;
import com.fbytes.llmka.service.Herald.impl.HeraldServiceTelegram;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class HeraldChannelConfig implements ApplicationListener<ContextRefreshedEvent> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${llmka.herald.news_group_header}")
    private String newsGroupHeader;
    @Value("${llmka.config.mappings_file}")
    private String mappingConfigFile;
    @Value("${llmka.herald.config_file}")
    private String heraldConfigFilePath;

    @Autowired
    private HeraldChannelFactory heraldChannelFactory;
    @Autowired
    private HeraldChannelConfigReader heraldChannelConfigReader;

    @Autowired
    private IntegrationFlowContext flowContext;
    @Autowired
    private MessageChannel heraldChannel;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        ConfigurableApplicationContext context = (ConfigurableApplicationContext) event.getApplicationContext();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        Environment environment = context.getEnvironment();

        Mapping[] mappings;

        try {
            String content = Files.readString(Path.of(mappingConfigFile));
            mappings = mapper.readValue(content, Mapping[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // create pub-sub channel for each uniue "outputChannel" in mappings
        Set<String> outputChannels = Arrays.stream(mappings).map(mapping -> mapping.getOutputChannel()).collect(Collectors.toSet());
        outputChannels.forEach(channelName -> {
            PublishSubscribeChannel channel = new PublishSubscribeChannel();
            channel.setBeanName(channelName);
            context.getBeanFactory().registerSingleton(channelName, channel);
        });

        // TODO: Make generic for other heralds
        // create herald services, subscribe them to channel with channel.name==herald.name
        List<HeraldChannel> heraldChannelTelegram = heraldChannelConfigReader.retrieveFromFile(heraldChannelFactory, new File(heraldConfigFilePath));
        for (HeraldChannel channel : heraldChannelTelegram) {
            if (!(channel instanceof HeraldChannelTelegram)) {
                throw new RuntimeException("Invalid configuration");
            }
            String botToken = environment.getProperty("llmka.herald.telegram.bot");
            HeraldServiceTelegram newHeraldService = new HeraldServiceTelegram((HeraldChannelTelegram) channel);
            beanFactory.autowireBean(newHeraldService);
            String beanName = StringUtils.capitalize(channel.getType().toLowerCase()) + "-" + channel.getName();
            beanFactory.initializeBean(newHeraldService, beanName);
            beanFactory.registerSingleton(beanName, newHeraldService);

            PublishSubscribeChannel channelBean = (PublishSubscribeChannel) beanFactory.getBean(channel.getName());
            channelBean.subscribe(message -> newHeraldService.sendMessage((String) message.getPayload()));
        }

        // Router
        HeaderValueRouter router = new HeaderValueRouter(newsGroupHeader);
        router.setChannelMappings(Stream.of(mappings).collect(Collectors.toMap(Mapping::getInputGroup, Mapping::getOutputChannel)));
        // TODO
        // router.setDefaultOutputChannel();
        beanFactory.registerSingleton("heraldRouter", router);

        IntegrationFlow heraldFlow = IntegrationFlow
                .from(heraldChannel)
                .<EmbeddedData, String>transform(embeddedData -> String.format("*%s* %s\t([%s](%s))",
                        embeddedData.getNewsData().getTitle(),
                        embeddedData.getNewsData().getDescription().orElse(""),
                        embeddedData.getNewsData().getDataSourceName(),
                        embeddedData.getNewsData().getLink()))
                .route(router)
                .get();
        beanFactory.registerSingleton("heraldFlow", heraldFlow);
        flowContext.registration(heraldFlow).register();
    }
}
