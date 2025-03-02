package com.fbytes.llmka.integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsCheckRejectReason;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.DataRetriver.IDataRetriever;
import com.fbytes.llmka.service.DataSource.IDataSourceConfigReader;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Herald.IHeraldService;
import com.fbytes.llmka.service.NewsDataCheck.INewsDataCheck;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
public class GatewayIntegrationConfig {
    @Value("${LLMka.datasource.config_folder}")
    private String configFolder;
    @Value("${LLMka.herald.telegram.bot.channel}")
    private String telegramChannel;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IDataSourceConfigReader dataSourceConfigReader;
    @Autowired
    private INewsDataCheck newsDataCheck;
    @Autowired
    private IHeraldService telegramBotService;

    @Autowired
    private PollerMetadata configPoller;

    private static final Logger logger = Logger.getLogger(GatewayIntegrationConfig.class);

    private static final String rejectReasonHeader = "RejectReason";
    private static final String rejectExplainHeader = "RejectExplain";


    @Bean
    public IntegrationFlow readDataSorcesConfig(@Qualifier("datasourceChannel") MessageChannel datasourceChannel) {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(configFolder))
                                .filter(new SimplePatternFileListFilter("*.cfg")),
                        config -> config.poller(configPoller)) // Poll every minute
                .handle((payload, headers) -> {
                    dataSourceConfigReader.retrieveDataSourcesFromFile((File) payload, item ->
                            datasourceChannel.send(MessageBuilder.withPayload(item).build()));
                    return null;
                })
                .get();
    }


    @Bean
    public IntegrationFlow processNewsData(IDataRetriever dataRetriever) {
        return IntegrationFlow
                .from("datasourceChannel")
                .handle(dataRetriever, "retrieveData")
                .filter((Optional<Stream<NewsData>> opt) -> !opt.isEmpty())
                .transform((Optional<Stream<NewsData>> opt) -> opt.get())
                .split()
                .channel("newDataChannelOut")
                .get();
    }


    @Bean
    public IntegrationFlow embeddingFlow(IEmbeddingService embeddingService) {
        return IntegrationFlow.from("embeddingChannel")
                .handle(embeddingService, "embedNewsData")
                .channel("embeddingChannelOut")
                .get();
    }


    @Bean
    public MessageSelector newsDataCheckSelector() {
        return new MessageSelector() {
            @Override
            public boolean accept(Message<?> message) {
                Pair<Boolean, Optional<NewsCheckRejectReason>> result = newsDataCheck.checkNewsData((EmbeddedData) message.getPayload());
                if (!result.getLeft()) {
                    Message<?> rejectedMessage = MessageBuilder.fromMessage(message)
                            .setHeader(rejectReasonHeader, result.getRight().get().getReason())
                            .setHeader(rejectExplainHeader, result.getRight().get().getExplain())
                            .build();
                    MessageChannel channel = applicationContext.getBean("newsDataCheckChannelReject", MessageChannel.class);
                    channel.send(rejectedMessage);
                }
                return result.getLeft();
            }
        };
    }


    @Bean
    public IntegrationFlow newsDataCheckFlow(INewsDataCheck newsDataCheckService) {
        return IntegrationFlow.from("newsDataCheckChannel")
                .filter(newsDataCheckSelector(), e -> e.discardChannel("nullChannel"))
                .handle(m -> {
                    // TODO: Replace with transformer
                    EmbeddedData embeddedData = (EmbeddedData) m.getPayload();
                    String messageStr = String.format("*%s*%s\t([%s](%s))",
                            embeddedData.getNewsData().getTitle(),
                            embeddedData.getNewsData().getDescription().orElse(""),
                            embeddedData.getNewsData().getDataSourceName(),
                            embeddedData.getNewsData().getLink());
                    logger.info("Sending message to {} {} bytes", telegramChannel, messageStr.getBytes().length);
                    telegramBotService.sendMessage(telegramChannel, messageStr);
                })
                .get();
    }


    @Bean
    public IntegrationFlow bridge1() {
        return IntegrationFlow
                .from("newDataChannelOut")
                .channel("embeddingChannel")
                .get();
    }

    @Bean
    public IntegrationFlow bridge2() {
        return IntegrationFlow
                .from("embeddingChannelOut")
                .channel("newsDataCheckChannel")
                .get();
    }

    @Bean
    public IntegrationFlow newsDataCheckChannelOutBind() {
        return IntegrationFlow
                .from("newsDataCheckChannelOut")
                .nullChannel();
    }

    @Bean
    public IntegrationFlow newsDataCheckChannelRejectBind() {
        return IntegrationFlow
                .from("newsDataCheckChannelReject")
                //.handle(m -> logger.info("Reject reason: {}  Message: {}", m.getHeaders().get(rejectReasonHeader), m.getPayload()))
                //.get();
                .nullChannel();
    }
}
