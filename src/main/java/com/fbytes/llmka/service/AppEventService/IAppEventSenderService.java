package com.fbytes.llmka.service.AppEventService;

import com.fbytes.llmka.model.appevent.AppEvent;

@FunctionalInterface
public interface IAppEventSenderService<T extends AppEvent> {
    void sendEvent(T event);
}
