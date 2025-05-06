package com.fbytes.llmka.integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.Optional;

public class NewsCheckDataSelector implements MessageSelector {
    private static final Logger logger = Logger.getLogger(NewsCheckDataSelector.class);

    @Value("${llmka.newscheck.reject.reject_reason_header}")
    private String rejectReasonHeader;
    @Value("${llmka.newscheck.reject.reject_explain_header}")
    private String rejectExplainHeader;

    @Autowired
    @Qualifier("newsCheckData")
    private INewsCheck newsCheckData;
    @Value("${llmka.herald.news_group_header}")
    private String newsGroupHeader;

    private final MessageChannel rejectChannel;


    public NewsCheckDataSelector(@Autowired MessageChannel rejectChannel) {
        this.rejectChannel = rejectChannel;
    }

    @Override
    public boolean accept(Message<?> message) {
        try {
            String schema = (String) message.getHeaders().get(newsGroupHeader);
            if (schema == null)
                throw new RuntimeException("NewsCheckDataSelector expects " + newsGroupHeader + " header to be set");
            Optional<INewsCheck.RejectReason> result = newsCheckData.checkNews(schema, (NewsData) message.getPayload());
            if (!result.isEmpty()) {
                Message<?> rejectedMessage = MessageBuilder.fromMessage(message)
                        .setHeader(rejectReasonHeader, result.get().getReason())
                        .setHeader(rejectExplainHeader, result.get().getExplain())
                        .build();
                rejectChannel.send(rejectedMessage);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.logException(e);
            throw e;
        }
    }
}
