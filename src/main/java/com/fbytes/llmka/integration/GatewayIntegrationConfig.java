package com.fbytes.llmka.integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.DataRetriver.IDataRetriever;
import com.fbytes.llmka.service.DataSource.IDataSourceConfigReader;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Herald.IHeraldService;
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
    @Value("${LLMka.datacheck.reject.reject_reason_header}")
    private String rejectReasonHeader;
    @Value("${LLMka.datacheck.reject.reject_explain_header}")
    private String rejectExplainHeader;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IDataSourceConfigReader dataSourceConfigReader;

    @Autowired
    private IHeraldService telegramBotService;

    @Autowired
    private PollerMetadata configPoller;

    @Autowired
    private PollerMetadata telegramPoller;

    private static final Logger logger = Logger.getLogger(GatewayIntegrationConfig.class);

    @Bean
    public IntegrationFlow readDataSorcesConfig(@Qualifier("datasourceChannel") MessageChannel datasourceChannel) {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(configFolder))
                                .filter(new SimplePatternFileListFilter("*.cfg")),
                        config -> config.poller(configPoller)) // Poll every minute
                .handle((payload, headers) -> {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
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
    public MessageSelector newsDataCheckSelector(@Qualifier("newsDataCheckChannelReject") MessageChannel rejectChannel) {
        NewsDataCheckSelector selector = new NewsDataCheckSelector(rejectChannel);
        return selector;
    }

    @Bean
    public IntegrationFlow newsDataCheckFlow(MessageSelector newsDataCheckSelector) {
        return IntegrationFlow.from("newsDataCheckChannel")
                .filter(newsDataCheckSelector, cfg -> cfg.discardChannel("nullChannel"))
                .channel("newsDataCheckChannelOut")
                .get();
    }

    @Bean
    public IntegrationFlow heraldFlow(@Autowired MessageChannel heraldChannel) {
        return IntegrationFlow.from(heraldChannel)
                .handle(m -> {
                    // TODO: Replace with transformer
                    EmbeddedData embeddedData = (EmbeddedData) m.getPayload();
                    String messageStr = String.format("*%s* %s\t([%s](%s))",
                            embeddedData.getNewsData().getTitle(),
                            embeddedData.getNewsData().getDescription().orElse(""),
                            embeddedData.getNewsData().getDataSourceName(),
                            embeddedData.getNewsData().getLink());
                    logger.info("Sending message to {} {} bytes", telegramChannel, messageStr.getBytes().length);
                    telegramBotService.sendMessage(telegramChannel, messageStr);
                }, config -> config.poller(telegramPoller))
                .get();
    }


    @Bean
    public IntegrationFlow bridgeNewDataChannelOut() {
        return IntegrationFlow
                .from("newDataChannelOut")
                .channel("embeddingChannel")
                .get();
    }

    @Bean
    public IntegrationFlow bridgeEmbeddingChannelOut() {
        return IntegrationFlow
                .from("embeddingChannelOut")
                .channel("newsDataCheckChannel")
                .get();
    }

    @Bean
    public IntegrationFlow bridgeNewsDataCheckChannelOut() {
        return IntegrationFlow
                .from("newsDataCheckChannelOut")
                .channel("heraldChannel")
                .get();
    }


    @Bean
    public IntegrationFlow newsDataCheckChannelRejectBind() {
        return IntegrationFlow
                .from("newsDataCheckChannelReject")
                .handle(m -> logger.info("Reject reason: {}  Message: {}", m.getHeaders().get(rejectReasonHeader), m.getPayload()))
                .get();
        //.nullChannel();
    }
}
