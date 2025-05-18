package com.fbytes.llmka.integration.steps;

import com.fbytes.llmka.integration.steps.selector.NewsCheckMetaSelector;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;

@Configuration
public class StepNewsCheckMeta {
    private static final Logger logger = Logger.getLogger(StepNewsCheckMeta.class);


    @Bean(name = "newsCheckMetaChannel")
    public MessageChannel newsCheckMetaChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean(name = "newsCheckMetaChannelOut")
    public MessageChannel newsCheckMetaChannelOut() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean(name = "newsCheckMetaSelector")
    public MessageSelector newsCheckMetaSelector(@Qualifier("newsCheckChannelReject") MessageChannel rejectChannel) {
        return new NewsCheckMetaSelector(rejectChannel);
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow newsCheckMetaFlow(MessageSelector newsCheckMetaSelector) {

        return org.springframework.integration.dsl.IntegrationFlow.from("newsCheckMetaChannel")
                .filter(newsCheckMetaSelector, cfg -> cfg.discardChannel("nullChannel"))
                .channel("newsCheckMetaChannelOut")
                .get();
    }
}
