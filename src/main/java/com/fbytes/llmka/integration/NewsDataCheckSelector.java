package com.fbytes.llmka.integration;

import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsCheckRejectReason;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.Optional;

public class NewsDataCheckSelector implements MessageSelector {

    @Value("${llmka.datacheck.reject.reject_reason_header}")
    private String rejectReasonHeader;
    @Value("${llmka.datacheck.reject.reject_explain_header}")
    private String rejectExplainHeader;

    @Autowired
    private INewsCheck newsDataCheck;
    @Value("${llmka.herald.news_group_header}")
    private String newsGroupHeader;

    private final MessageChannel rejectChannel;


    public NewsDataCheckSelector(@Autowired MessageChannel rejectChannel) {
        this.rejectChannel = rejectChannel;
    }

    @Override
    public boolean accept(Message<?> message) {
        String schema = (String) message.getHeaders().get(newsGroupHeader);
        if (schema == null)
            throw new RuntimeException("NewsCheckSelector extects " + newsGroupHeader + " header to be set");
        Optional<NewsCheckRejectReason> result = newsDataCheck.checkNews(schema, (EmbeddedData) message.getPayload());
        if (!result.isEmpty()) {
            Message<?> rejectedMessage = MessageBuilder.fromMessage(message)
                    .setHeader(rejectReasonHeader, result.get().getReason())
                    .setHeader(rejectExplainHeader, result.get().getExplain())
                    .build();
            rejectChannel.send(rejectedMessage);
            return false;
        }
        return true;
    }
}
