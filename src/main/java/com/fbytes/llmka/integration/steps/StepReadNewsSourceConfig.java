package com.fbytes.llmka.integration.steps;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.config.newssource.NewsSource;
import com.fbytes.llmka.model.config.newssource.NewsSourceFactory;
import com.fbytes.llmka.service.ConfigReader.impl.ConfigReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

import java.io.File;

@Configuration
public class StepReadNewsSourceConfig {
    private static final Logger logger = Logger.getLogger(StepReadNewsSourceConfig.class);

    @Value("${llmka.newssource.config_folder}")
    private String configFolder;

    @Autowired
    private PollerMetadata configPoller;
    @Autowired
    private ConfigReader<NewsSource> newsSourceConfigReader;
    @Autowired
    private NewsSourceFactory newsSourceFactory;


    @Bean
    public IntegrationFlow readNewsSourcesConfig(@Qualifier("newsSourceChannel") MessageChannel newsSourceChannel) {
        return IntegrationFlow
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
}
