package com.fbytes.llmka.integration.steps;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.NewsProcessor.impl.NewsProcessorLastSentence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class StepLastSentence {
    @Bean(name = "lastSentenceChannel")
    public MessageChannel lastSentenceChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean(name = "lastSentenceChannelOut")
    public MessageChannel lastSentenceChannelOut() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow lastSentencefFlow(NewsProcessorLastSentence lastSentenceProcessor) {
        return org.springframework.integration.dsl.IntegrationFlow.from("lastSentenceChannel")
                .handle(lastSentenceProcessor, "process")
                .channel("lastSentenceChannelOut")
                .get();
    }
}
