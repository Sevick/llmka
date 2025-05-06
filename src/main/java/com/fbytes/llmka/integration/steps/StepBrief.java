package com.fbytes.llmka.integration.steps;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.NewsProcessor.impl.NewsProcessorBrief;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class StepBrief {
    @Bean(name = "briefChannel")
    public MessageChannel briefChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean(name = "briefChannelOut")
    public MessageChannel briefChannelOut() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow briefFlow(NewsProcessorBrief briefNewsProcessor) {
        return org.springframework.integration.dsl.IntegrationFlow.from("briefChannel")
                .handle(briefNewsProcessor, "process")
                .channel("briefChannelOut")
                .get();
    }
}
