package com.fbytes.llmka.service.Maintenance.AppEventSenderService;

import com.fbytes.llmka.model.appevent.AppEvent;

@FunctionalInterface
public interface IAppEventSenderService<T extends AppEvent> {
    void sendEvent(T event);
}
