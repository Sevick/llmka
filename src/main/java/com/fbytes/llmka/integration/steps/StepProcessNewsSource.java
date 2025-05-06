package com.fbytes.llmka.integration.steps;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.config.newssource.NewsSource;
import com.fbytes.llmka.service.DataRetrieverService.IDataRetrieveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

import java.util.Optional;
import java.util.stream.Stream;

@Configuration
public class StepProcessNewsSource {
    private static final Logger logger = Logger.getLogger(StepProcessNewsSource.class);

    @Value("${llmka.herald.news_group_header}")
    private String newsGroupHeader;
    @Value("${llmka.newssource_header}")
    private String newsSourceHeader;
    @Value("${llmka.newsdata_header}")
    private String newsDataHeader;


    @Bean(name = "newsSourceChannel")
    public MessageChannel newsSourceChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsSource.class);
        return channel;
    }

    @Bean(name = "newsDataChannelOut")
    public MessageChannel newsDataChannelOut() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow processNewsSource(@Autowired @Qualifier("dataRetrieveService") IDataRetrieveService dataRetrieveService) {
        return IntegrationFlow
                .from("newsSourceChannel")
                .enrichHeaders(h -> h
                        .headerFunction(newsGroupHeader,m -> ((NewsSource) m.getPayload()).getGroup())
                        .headerFunction(newsSourceHeader, m -> ((NewsSource) m.getPayload()).getName())
                )
                .handle(dataRetrieveService, "retrieveData")
                .filter((Optional<Stream<NewsData>> opt) -> !opt.isEmpty())
                .transform((Optional<Stream<NewsData>> opt) -> opt.get())
                .split()    // headers copied to each message
                .enrichHeaders(h -> h
                        .headerFunction(newsDataHeader,m -> ((NewsData) m.getPayload()).getId())
                )
                .channel("newsDataChannelOut")
                .get();
    }
}
