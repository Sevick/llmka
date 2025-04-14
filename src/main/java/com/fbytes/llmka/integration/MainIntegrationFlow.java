package com.fbytes.llmka.integration;

import com.fbytes.llmka.logger.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSelector;

@Configuration
public class MainIntegrationFlow {
    private static final Logger logger = Logger.getLogger(MainIntegrationFlow.class);

    @Value("${llmka.newscheck.reject.reject_reason_header}")
    private String rejectReasonHeader;
    @Value("${llmka.newscheck.reject.reject_explain_header}")
    private String rejectExplainHeader;


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeCheckMeta() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsDataChannelOut")
                .channel("newsCheckChannel")
                .get();
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeBrief() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsCheckChannelOut")
                .channel("briefChannel")
                .get();
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeEmbedding() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("briefChannelOut")
                .channel("heraldChannel")
                .get();
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow newsDataCheckChannelRejectBind() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsCheckChannelReject")
                .handle(m -> logger.info("Reject Message:\n{}\nReason: {}{}",
                                m,
                                m.getHeaders().get(rejectReasonHeader),
                                m.getHeaders().get(rejectExplainHeader) == null ? "" : String.format("%nExplain: %s", m.getHeaders().get(rejectExplainHeader))
                        )
                )
                .get();
        //.nullChannel();
    }
}
