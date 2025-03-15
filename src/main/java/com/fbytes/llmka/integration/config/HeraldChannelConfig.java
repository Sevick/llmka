package com.fbytes.llmka.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbytes.llmka.integration.service.HeraldConfigService;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.Mapping;
import com.fbytes.llmka.model.heraldchannel.Herald;
import com.fbytes.llmka.model.heraldchannel.HeraldTelegram;
import com.fbytes.llmka.model.heraldmessage.HeraldMessage;
import com.fbytes.llmka.model.heraldmessage.TelegramMessage;
import com.fbytes.llmka.service.Herald.IHeraldService;
import com.fbytes.llmka.service.Herald.impl.HeraldServiceTelegram;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.support.management.micrometer.MicrometerMetricsCaptor;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class HeraldChannelConfig implements ApplicationListener<ContextRefreshedEvent> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${llmka.herald.news_group_header}")
    private String newsGroupHeader;
    @Value("${llmka.herald.default_queue_size}")
    private Integer defaultQueueSize;
    @Value("${llmka.config.mappings_file}")
    private String mappingConfigFile;

    @Autowired
    private IntegrationFlowContext flowContext;
    @Autowired
    private MessageChannel heraldChannel;
    @Autowired
    private HeraldConfigService heraldConfigService;
    @Autowired
    private PollerMetadata telegramPoller;
    @Autowired
    private MeterRegistry meterRegistry;

    private final GenericApplicationContext applicationContext;

    public HeraldChannelConfig(@Autowired ApplicationContext applicationContext) {
        this.applicationContext = (GenericApplicationContext) applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        ConfigurableApplicationContext context = (ConfigurableApplicationContext) event.getApplicationContext();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();

        Mapping[] mappings;

        try {
            String content = Files.readString(Path.of(mappingConfigFile));
            mappings = mapper.readValue(content, Mapping[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // create pub-sub channel for each unique "outputChannel" in mappings
        Set<String> outputChannels = Arrays.stream(mappings).map(mapping -> mapping.getOutputChannel()).collect(Collectors.toSet());
        outputChannels.forEach(channelName -> {
            PublishSubscribeChannel channel = new PublishSubscribeChannel();
            channel.setBeanName(channelName);
            context.getBeanFactory().registerSingleton(channelName, channel);
        });

        // TODO: Make generic for other heralds
        // create herald services
        Map<String, List<Pair<String, IHeraldService>>> outchannelToHeraldServiceMap = new HashMap<>(); // <outputChannel, <HeraldBeanName, IHeraldService>>
        for (Herald herald : heraldConfigService.getHeralds()) {
            if (!(herald instanceof HeraldTelegram)) {
                throw new RuntimeException("Invalid configuration");
            }
            HeraldServiceTelegram newHeraldService = new HeraldServiceTelegram((HeraldTelegram) herald);   // replace with factory?
            beanFactory.autowireBean(newHeraldService);
            String beanName = StringUtils.capitalize(herald.getType().toLowerCase()) + "-" + herald.getName();
            beanFactory.initializeBean(newHeraldService, beanName);
            beanFactory.registerSingleton(beanName, newHeraldService);
            Pair<String, IHeraldService> newHeraldPair = Pair.of(beanName, newHeraldService);
            if (outchannelToHeraldServiceMap.containsKey(herald.getChannel())) {
                outchannelToHeraldServiceMap.get(herald.getChannel()).add(newHeraldPair);
            } else {
                List<Pair<String, IHeraldService>> heraldServiceList = new ArrayList<>();
                heraldServiceList.add(newHeraldPair);
                outchannelToHeraldServiceMap.put(herald.getChannel(), heraldServiceList);
            }


            QueueChannel queueChannel = new QueueChannel(defaultQueueSize);
            applicationContext.registerBean(heraldQueueName(beanName), QueueChannel.class, () -> queueChannel);

            // TODO: Add captor
            //queueChannel.registerMetricsCaptor(new MicrometerMetricsCaptor(meterRegistry));

            BridgeHandler bridgeHandler = new BridgeHandler();
            bridgeHandler.setOutputChannel(queueChannel);

            PublishSubscribeChannel outChannel = ((PublishSubscribeChannel) beanFactory.getBean(herald.getChannel()));
            outChannel.subscribe(bridgeHandler);
        }


        // bind heralds to corresponding output channels
        outchannelToHeraldServiceMap.entrySet().forEach(entry -> {
            //PublishSubscribeChannel outChannel = ((PublishSubscribeChannel) beanFactory.getBean(entry.getKey()));
            entry.getValue().forEach(heraldNameService -> {
                String heraldQName = heraldQueueName(heraldNameService.getLeft());
                QueueChannel heraldQueueChannel = ((QueueChannel) beanFactory.getBean(heraldQName));

                IntegrationFlow heraldQFlow = IntegrationFlow
                        .from(heraldQueueChannel)
                        .handle(message -> heraldNameService.getRight().sendMessage(new TelegramMessage((String) message.getPayload())),
                                e -> e.poller(telegramPoller))
                        .get();
                beanFactory.registerSingleton(heraldQName+"-Flow", heraldQFlow);
                flowContext.registration(heraldQFlow).register();
            });
        });


        // Router (route messages from heraldChannel to appropriate pub-sub)
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

    private String heraldQueueName(String heraldBeanName) {
        return heraldBeanName + "-Q";
    }


//    @Bean
//    public IntegrationFlow bufferingWorkflow() {
//        return IntegrationFlow.from("pubSubChannel")
//                .delay("bufferingDelayer", d -> d.defaultDelay(1000)) // 1-second delay
//                .channel("queueChannel")
//                .get();
//    }
}
