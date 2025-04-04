package com.fbytes.llmka.integration.steps;

import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StepEmbedding {
    @Bean
    public org.springframework.integration.dsl.IntegrationFlow embeddingFlow(IEmbeddingService embeddingService) {
        return org.springframework.integration.dsl.IntegrationFlow.from("embeddingChannel")
                .handle(embeddingService, "embedNewsData")
                .channel("embeddingChannelOut")
                .get();
    }
}
