package com.fbytes.llmka.integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.newssource.NewsSource;
import com.fbytes.llmka.model.newssource.NewsSourceFactory;
import com.fbytes.llmka.service.ConfigReader.impl.ConfigReader;
import com.fbytes.llmka.service.DataRetriver.IDataRetriever;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
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
public class MainIntegrationFlow {
    private static final Logger logger = Logger.getLogger(MainIntegrationFlow.class);

    @Value("${llmka.newssource.config_folder}")
    private String configFolder;
    @Value("${llmka.datacheck.reject.reject_reason_header}")
    private String rejectReasonHeader;
    @Value("${llmka.datacheck.reject.reject_explain_header}")
    private String rejectExplainHeader;
    @Value("${llmka.herald.news_group_header}")
    private String newsGroupHeader;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private PollerMetadata configPoller;
    @Autowired
    ConfigReader<NewsSource> newsSourceConfigReader;
    @Autowired
    NewsSourceFactory newsSourceFactory;


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow readDataSorcesConfig(@Qualifier("newsSourceChannel") MessageChannel newsSourceChannel) {
        return org.springframework.integration.dsl.IntegrationFlow
                .from(Files.inboundAdapter(new File(configFolder))
                                .filter(new SimplePatternFileListFilter("*.cfg")),
                        config -> config.poller(configPoller)) // Poll every minute
//                .enrichHeaders(h -> h.headerFunction(newsGroupHeader,
//                        m -> ((File) m.getPayload()).getName()))
                .handle((payload, headers) -> {
                    newsSourceConfigReader.retrieveFromFile(newsSourceFactory, (File) payload, item ->
                            newsSourceChannel.send(
                                    MessageBuilder
                                            .withPayload(item)
                                            .copyHeaders(headers)
                                            .build()
                            )
                    );
                    return null;
                })
                .get();
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow processNewsData(IDataRetriever dataRetriever) {
        return IntegrationFlow
                .from("newsSourceChannel")
                .enrichHeaders(h -> h.headerFunction(newsGroupHeader,
                        m -> ((NewsSource) m.getPayload()).getGroup()))
                .handle(dataRetriever, "retrieveData")
                .filter((Optional<Stream<NewsData>> opt) -> !opt.isEmpty())
                .transform((Optional<Stream<NewsData>> opt) -> opt.get())
                .split()
                .channel("newsDataChannelOut")
                .get();
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow embeddingFlow(IEmbeddingService embeddingService) {
        return org.springframework.integration.dsl.IntegrationFlow.from("embeddingChannel")
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
    public org.springframework.integration.dsl.IntegrationFlow newsDataCheckFlow(MessageSelector newsDataCheckSelector) {
        return org.springframework.integration.dsl.IntegrationFlow.from("newsDataCheckChannel")
                .filter(newsDataCheckSelector, cfg -> cfg.discardChannel("nullChannel"))
                .channel("newsDataCheckChannelOut")
                .get();
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeNewDataChannelOut() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsDataChannelOut")
                .channel("embeddingChannel")
                .get();
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeEmbeddingChannelOut() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("embeddingChannelOut")
                .channel("newsDataCheckChannel")
                .get();
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeNewsDataCheckChannelOut() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsDataCheckChannelOut")
                .channel("heraldChannel")
                .get();
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow newsDataCheckChannelRejectBind() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsDataCheckChannelReject")
                .handle(m -> logger.info("Reject Message:\n{}\nReason: {}{}", ((EmbeddedData) m.getPayload()).getNewsData(),
                        m.getHeaders().get(rejectReasonHeader),
                        m.getHeaders().get(rejectExplainHeader) == null ? "" : String.format("\nExplain: %s", m.getHeaders().get(rejectExplainHeader))
                        )
                )
                .get();
        //.nullChannel();
    }
}
