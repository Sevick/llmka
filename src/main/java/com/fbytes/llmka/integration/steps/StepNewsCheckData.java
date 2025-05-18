package com.fbytes.llmka.integration.steps;

import com.fbytes.llmka.integration.steps.selector.NewsCheckDataSelector;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;

@Configuration
public class StepNewsCheckData {
    private static final Logger logger = Logger.getLogger(StepNewsCheckData.class);

    @Bean(name = "newsCheckDataChannel")
    public MessageChannel newsCheckDataChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean(name = "newsCheckDataChannelOut")
    public MessageChannel newsCheckDataChannelOut() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean(name = "newsCheckDataSelector")
    public MessageSelector newsCheckDataSelector(@Qualifier("newsCheckChannelReject") MessageChannel rejectChannel) {
        return new NewsCheckDataSelector(rejectChannel);
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow newsCheckDataFlow(@Qualifier("newsCheckDataSelector") MessageSelector newsCheckDataSelector) {

        return org.springframework.integration.dsl.IntegrationFlow.from("newsCheckDataChannel")
                .filter(newsCheckDataSelector, cfg -> cfg.discardChannel("nullChannel"))
                .channel("newsCheckDataChannelOut")
                .get();
    }
}
