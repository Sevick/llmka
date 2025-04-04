package com.fbytes.llmka.integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.EmbeddedData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MainIntegrationFlow {
    private static final Logger logger = Logger.getLogger(MainIntegrationFlow.class);

    @Value("${llmka.datacheck.reject.reject_reason_header}")
    private String rejectReasonHeader;
    @Value("${llmka.datacheck.reject.reject_explain_header}")
    private String rejectExplainHeader;


    @Bean
    public MessageSelector newsDataCheckSelector(@Qualifier("newsDataCheckChannelReject") MessageChannel rejectChannel) {
        return new NewsDataCheckSelector(rejectChannel);
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow newsDataCheckFlow(MessageSelector newsDataCheckSelector) {
        return org.springframework.integration.dsl.IntegrationFlow.from("newsDataCheckChannel")
                .filter(newsDataCheckSelector, cfg -> cfg.discardChannel("nullChannel"))
                .channel("newsDataCheckChannelOut")
                .get();
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeNewDataChannelOut() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsDataChannelOut")
                .channel("embeddingChannel")
                .get();
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeEmbeddingChannelOut() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("embeddingChannelOut")
                .channel("newsDataCheckChannel")
                .get();
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeNewsDataCheckChannelOut() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsDataCheckChannelOut")
                .channel("heraldChannel")
                .get();
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow newsDataCheckChannelRejectBind() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsDataCheckChannelReject")
                .handle(m -> logger.info("Reject Message:\n{}\nReason: {}{}", ((EmbeddedData) m.getPayload()).getNewsData(),
                                m.getHeaders().get(rejectReasonHeader),
                                m.getHeaders().get(rejectExplainHeader) == null ? "" : String.format("\nExplain: %s", m.getHeaders().get(rejectExplainHeader))
                        )
                )
                .get();
        //.nullChannel();
    }
}
