package com.fbytes.llmka.integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.text.MessageFormat;

@Configuration
public class MainIntegrationFlow {
    private static final Logger logger = Logger.getLogger(MainIntegrationFlow.class);

    @Value("${llmka.newscheck.reject.reject_reason_header}")
    private String rejectReasonHeader;
    @Value("${llmka.newscheck.reject.reject_explain_header}")
    private String rejectExplainHeader;
    @Value("${llmka.newsdata_header}")
    private String newsDataHeader;

    @Value("${llmka.brief.description_size_limit}")
    private Integer descriptionSizeLimit;

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeDataCheck() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsDataChannelOut")
                .handle((payload, headers) -> {
                    MDC.put(newsDataHeader, ((NewsData) payload).getId());
                    ThreadContext.put(newsDataHeader, ((NewsData) payload).getId());
                    return MessageBuilder
                            .withPayload(payload)
                            .copyHeaders(headers)
                            .build();
                })
                .channel("newsCheckMetaChannel")
                .get();
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeMetaCheck() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsCheckMetaChannelOut")
                .channel("newsCheckDataChannel")
                .get();
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeCheckData() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsCheckDataChannelOut")
                .channel("newsCheckAdChannel")
                .get();
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeCheckAd() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsCheckAdChannelOut")
                .route(Message.class, msg -> {
                    if (((NewsData) msg.getPayload()).getDescription().get().length() > descriptionSizeLimit) {
                        return "briefChannel";
                    } else {
                        return "lastSentenceChannel";
                    }
                })
                .get();
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeLastSentence() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("lastSentenceChannelOut")
                .channel("heraldChannel_Q")
                .get();
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow bridgeBrief() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("briefChannelOut")
                .channel("heraldChannel_Q")
                .get();
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow newsDataCheckChannelRejectBind() {
        return org.springframework.integration.dsl.IntegrationFlow
                .from("newsCheckChannelReject")
                .filter((MessageSelector) message -> message.getHeaders().get(rejectReasonHeader).equals("META_DUPLICATION"))
                .handle(m -> logger.info("Reject Message:\n{}\nReason: {}{}",
                                m,
                                m.getHeaders().get(rejectReasonHeader),
                                m.getHeaders().get(rejectExplainHeader) == null ? "" : MessageFormat.format("\nExplain: {0}", m.getHeaders().get(rejectExplainHeader))
                        )
                )
                .get();
                //.nullChannel();
    }
}
