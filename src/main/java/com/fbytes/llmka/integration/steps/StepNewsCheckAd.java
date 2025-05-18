package com.fbytes.llmka.integration.steps;

import com.fbytes.llmka.integration.steps.selector.NewsCheckAdSelector;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;

@Configuration
public class StepNewsCheckAd {
    private static final Logger logger = Logger.getLogger(StepNewsCheckAd.class);

    @Bean(name = "newsCheckAdChannel")
    public MessageChannel newsCheckAdChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean(name = "newsCheckAdChannelOut")
    public MessageChannel newsCheckAdChannelOut() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean(name = "newsCheckAdSelector")
    public MessageSelector newsCheckAdSelector(@Qualifier("newsCheckChannelReject") MessageChannel rejectChannel) {
        return new NewsCheckAdSelector(rejectChannel);
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow newsCheckAdFlow(@Qualifier("newsCheckAdSelector") MessageSelector newsCheckAdSelector) {

        return org.springframework.integration.dsl.IntegrationFlow.from("newsCheckAdChannel")
                .filter(newsCheckAdSelector, cfg -> cfg.discardChannel("nullChannel"))
                .channel("newsCheckAdChannelOut")
                .get();
    }
}
