package com.fbytes.llmka.integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.NewsSourceConfigReader.INewsSourceConfigReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class NewsSourceReaderIntegration {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private INewsSourceConfigReader newsSourceConfigReader;

    private static final Logger logger = Logger.getLogger(NewsSourceReaderIntegration.class);

    public void readNewsSourceConfig(String groupName, InputStream inputStream) {
        MessageChannel newsSourceChannel = applicationContext.getBean("newsSourceChannel", MessageChannel.class);
        try {
            newsSourceConfigReader.retrieveNewsSources(groupName, inputStream, item ->
                    newsSourceChannel.send(MessageBuilder.withPayload(item).build()));
        } catch (Exception e) {
            logger.logException(e);                 // TODO - send message / circuit breaker
        }
    }
}
