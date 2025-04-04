package com.fbytes.llmka.integration.steps;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.newssource.NewsSource;
import com.fbytes.llmka.service.DataRetriver.IDataRetrieveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;

import java.util.Optional;
import java.util.stream.Stream;

@Configuration
public class StepProcessNewsSource {
    private static final Logger logger = Logger.getLogger(StepProcessNewsSource.class);

    @Value("${llmka.herald.news_group_header}")
    private String newsGroupHeader;

    @Autowired
    @Qualifier("DataRetrieveService")
    IDataRetrieveService dataRetrieveService;

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow processNewsData() {
        return IntegrationFlow
                .from("newsSourceChannel")
                .enrichHeaders(h -> h.headerFunction(newsGroupHeader,
                        m -> ((NewsSource) m.getPayload()).getGroup()))
                .handle(dataRetrieveService, "retrieveData")
                .filter((Optional<Stream<NewsData>> opt) -> !opt.isEmpty())
                .transform((Optional<Stream<NewsData>> opt) -> opt.get())
                .split()
                .channel("newsDataChannelOut")
                .get();
    }
}
