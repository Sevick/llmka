package com.fbytes.llmka.integration.steps;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.config.newssource.NewsSource;
import com.fbytes.llmka.model.config.newssource.NewsSourceFactory;
import com.fbytes.llmka.service.ConfigReader.ConfigReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;

import java.io.File;
import java.util.List;

@Configuration
public class StepReadNewsSourceConfig {
    private static final Logger logger = Logger.getLogger(StepReadNewsSourceConfig.class);

    @Value("${llmka.newssource.config_folder}")
    private String configFolder;

    @Autowired
    @Qualifier("newsSourcePoller")
    private PollerMetadata newsSourcePoller;
    @Autowired
    private ConfigReader<NewsSource> newsSourceConfigReader;
    @Autowired
    private NewsSourceFactory newsSourceFactory;


    @Bean
    public IntegrationFlow readNewsSourcesConfig(@Qualifier("newsSourceChannel") MessageChannel newsSourceChannel) {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(configFolder))
                                .filter(new SimplePatternFileListFilter("*.cfg")),
                        config -> config.poller(newsSourcePoller))
                .handle((payload, headers) -> {
                    List<NewsSource> newsSourceList = newsSourceConfigReader.retrieveFromFile(newsSourceFactory, (File) payload);
                    return newsSourceList.toArray();
                })
                .split()
                .channel(newsSourceChannel)
                .get();
    }
}
