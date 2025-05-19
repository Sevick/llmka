package com.fbytes.llmka.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbytes.llmka.integration.service.IHeraldFactory;
import com.fbytes.llmka.integration.service.IHeraldPollerFactory;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.config.HeraldsConfiguration;
import com.fbytes.llmka.model.config.Mapping;
import com.fbytes.llmka.model.config.heraldchannel.HeraldConfig;
import com.fbytes.llmka.model.config.heraldchannel.HeraldConfigTelegram;
import com.fbytes.llmka.model.heraldmessage.HeraldMessage;
import com.fbytes.llmka.model.heraldmessage.TelegramMessage;
import com.fbytes.llmka.service.Herald.Herald;
import com.fbytes.llmka.service.Herald.IHerald;
import com.fbytes.llmka.service.Herald.IHeraldNameService;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.integration.router.HeaderValueRouter;
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
    private static final Logger logger = Logger.getLogger(HeraldChannelConfig.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${llmka.herald.news_group_header}")
    private String newsGroupHeader;
    @Value("${llmka.herald.default_queue_size}")
    private Integer defaultQueueSize;
    @Value("${llmka.config.mappings_file}")
    private String mappingConfigFile;
    @Value("${llmka.herald.silence:false}")
    private Boolean silence;

    @Autowired
    private IntegrationFlowContext flowContext;
    @Autowired
    @Qualifier("heraldChannel_Q")
    private MessageChannel heraldChannel;
    @Autowired
    private HeraldsConfiguration heraldsConfiguration;
    @Autowired
    private IHeraldFactory heraldFactory;
    @Autowired
    private IHeraldPollerFactory heraldPollerFactory;
    @Autowired
    private MeterRegistry meterRegistry;
    @Autowired
    @Qualifier("pubsubExecutor")
    private TaskExecutor pubsubExecutor;

    private final GenericApplicationContext applicationContext;

    public HeraldChannelConfig(@Autowired ApplicationContext applicationContext) {
        this.applicationContext = (GenericApplicationContext) applicationContext;
    }


    private void createOutputChannels(Mapping[] mappings, ConfigurableApplicationContext context) {
        // create pub-sub channel for each unique "outputChannel" in mappings
        Set<String> outputChannels = Arrays.stream(mappings).map(mapping -> mapping.getOutputChannel()).collect(Collectors.toSet());
        logger.info("Creating output channels: {}", outputChannels.size());
        outputChannels.forEach(channelName -> {
            PublishSubscribeChannel channel = new PublishSubscribeChannel(pubsubExecutor);
            channel.setDatatypes(NewsData.class);
            applicationContext.registerBean(channelName, PublishSubscribeChannel.class, () -> channel);
            //context.getBeanFactory().registerSingleton(channelName, channel);
            logger.info("Created output channel: {}", channelName);
        });
    }

    private Map<String, List<Pair<String, IHerald>>> createHeraldServices() {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        // TODO: Make generic for other heralds
        // create herald services
        Map<String, List<Pair<String, IHerald>>> outchannelToHeraldServiceMap = new HashMap<>(); // <outputChannel, <HeraldBeanName, IHeraldService>>
        logger.info("Creating herald services: {}", heraldsConfiguration.getHeraldConfigs().length);
        for (HeraldConfig heraldConfig : heraldsConfiguration.getHeraldConfigs()) {
            logger.info("Creating herald service for channel: {}", heraldConfig.getChannel());
            if (!(heraldConfig instanceof HeraldConfigTelegram)) {
                throw new RuntimeException("Invalid configuration");
            }
            Herald<HeraldMessage> createdHeraldService = heraldFactory.createHeraldService(heraldConfig);
            Pair<String, IHerald> newHeraldPair = Pair.of(IHeraldNameService.makeFullName(heraldConfig), createdHeraldService);

            outchannelToHeraldServiceMap.computeIfAbsent(heraldConfig.getChannel(), key -> new ArrayList<>())
                    .add(newHeraldPair);

            PollableChannel heraldChannel = new QueueChannel(defaultQueueSize);
            String qName = heraldQueueName(IHeraldNameService.makeFullName(heraldConfig));
            applicationContext.registerBean(qName, PollableChannel.class, () -> heraldChannel);
            //applicationContext.getAutowireCapableBeanFactory().autowireBean(applicationContext.getBean(qName));

            // TODO: Add captor
            //queueChannel.registerMetricsCaptor(new MicrometerMetricsCaptor(meterRegistry));

            BridgeHandler bridgeHandler = new BridgeHandler();
            bridgeHandler.setOutputChannel(heraldChannel);

            PublishSubscribeChannel outChannel = ((PublishSubscribeChannel) beanFactory.getBean(heraldConfig.getChannel()));
            outChannel.subscribe(bridgeHandler);
        }
        return outchannelToHeraldServiceMap;
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

        createOutputChannels(mappings, context);
        Map<String, List<Pair<String, IHerald>>> outchannelToHeraldServiceMap = createHeraldServices();

        // bind heralds to corresponding output channels
        outchannelToHeraldServiceMap.entrySet().forEach(entry -> {
            entry.getValue().forEach(herald -> {
                String heraldQName = heraldQueueName(herald.getLeft());
                PollableChannel heraldQueueChannel = ((PollableChannel) beanFactory.getBean(heraldQName));

                IntegrationFlow heraldQFlow = IntegrationFlow
                        .from(heraldQueueChannel)
                        .handle(message -> {
                                    try {
                                        herald.getRight().sendMessage(TelegramMessage.fromNewsData((NewsData) message.getPayload()));
                                    } catch (IHerald.SendMessageException e) {
                                        heraldQueueChannel.send(message);   // requeue
                                    }
                                },
                                e -> {
                                    e.poller(heraldPollerFactory.createHeraldPollerService(herald.getLeft()));
                                    //e.advice(mdcAdvice);
                                }
                        )
                        .get();
                applicationContext.registerBean(heraldQName + "-Flow", IntegrationFlow.class, () -> heraldQFlow);
                //beanFactory.registerSingleton(heraldQName + "-Flow", heraldQFlow);
                flowContext.registration(heraldQFlow).register();
            });
        });


        // Router (route messages from heraldChannel to appropriate pub-sub)
        HeaderValueRouter router = new HeaderValueRouter(newsGroupHeader);
        router.setChannelMappings(Stream.of(mappings).collect(Collectors.toMap(Mapping::getInputGroup, Mapping::getOutputChannel)));
        // TODO: router.setDefaultOutputChannel();
        applicationContext.registerBean("heraldRouter", HeaderValueRouter.class, () -> router);
        //beanFactory.registerSingleton("heraldRouter", router);

        IntegrationFlow heraldFlow;
        if (silence) {
            heraldFlow = IntegrationFlow
                    .from(heraldChannel)
                    .nullChannel();
        } else {
            heraldFlow = IntegrationFlow
                    .from(heraldChannel)
                    .route(router)
                    .get();
        }
        applicationContext.registerBean("herald-Flow", IntegrationFlow.class, () -> heraldFlow);
        flowContext.registration(heraldFlow).register();
    }

    private String heraldQueueName(String heraldBeanName) {
        return heraldBeanName + "_Q";
    }
}
