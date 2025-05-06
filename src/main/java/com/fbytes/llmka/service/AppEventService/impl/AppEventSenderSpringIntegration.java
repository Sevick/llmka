package com.fbytes.llmka.service.AppEventService.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.appevent.AppEvent;
import com.fbytes.llmka.service.AppEventService.AppEventSenderService;
import com.fbytes.llmka.service.NewsCheck.impl.NewsCheckMetaSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Service
public class AppEventSenderSpringIntegration<T extends AppEvent> extends AppEventSenderService<T> {
    private static final Logger logger = Logger.getLogger(AppEventSenderSpringIntegration.class);

    @Autowired
    @Qualifier("appEventChannel")
    private MessageChannel appEventChannel;

    @Override
    public void sendEvent(T event) {
        logger.debug("Sending event: {}", event);
        appEventChannel.send(MessageBuilder.withPayload(event).build());
    }
}
