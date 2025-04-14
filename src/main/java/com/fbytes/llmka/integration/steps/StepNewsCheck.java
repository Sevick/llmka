package com.fbytes.llmka.integration.steps;

import com.fbytes.llmka.integration.NewsCheckAdSelector;
import com.fbytes.llmka.integration.NewsCheckDataSelector;
import com.fbytes.llmka.integration.NewsCheckMetaSelector;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;

@Configuration
public class StepNewsCheck {
    private static final Logger logger = Logger.getLogger(StepNewsCheck.class);

    @Bean(name = "newsCheckChannel")
    public MessageChannel newsMetaCheckChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean(name = "newsCheckChannelOut")
    public MessageChannel newsMetaCheckChannelOut() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean(name = "newsCheckChannelReject")
    public MessageChannel newsDataCheckChannelReject() {
        return new DirectChannel();
    }


    @Bean
    public MessageSelector newsCheckMetaSelector(@Qualifier("newsCheckChannelReject") MessageChannel rejectChannel) {
        return new NewsCheckMetaSelector(rejectChannel);
    }

    @Bean
    public MessageSelector newsCheckDataSelector(@Qualifier("newsCheckChannelReject") MessageChannel rejectChannel) {
        return new NewsCheckDataSelector(rejectChannel);
    }

    @Bean
    public MessageSelector newsCheckAdSelector(@Qualifier("newsCheckChannelReject") MessageChannel rejectChannel) {
        return new NewsCheckAdSelector(rejectChannel);
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow newsCheckFlow(
            MessageSelector newsCheckMetaSelector, MessageSelector newsCheckDataSelector, MessageSelector newsCheckAdSelector) {

        return org.springframework.integration.dsl.IntegrationFlow.from("newsCheckChannel")
                .filter(newsCheckMetaSelector, cfg -> cfg.discardChannel("nullChannel"))
                .filter(newsCheckDataSelector, cfg -> cfg.discardChannel("nullChannel"))
                .filter(newsCheckAdSelector, cfg -> cfg.discardChannel("nullChannel"))
                .channel("newsCheckChannelOut")
                .get();
    }
}
