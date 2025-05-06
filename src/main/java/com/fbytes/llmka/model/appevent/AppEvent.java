package com.fbytes.llmka.model.appevent;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
public abstract class AppEvent {

    public enum EventType {METAHASH_COMPRESS}

    private String id = UUID.randomUUID().toString();
    private long timestamp = System.currentTimeMillis();

    private String service;
    private String instance;
    private EventType eventType;

    protected AppEvent(String service, String instance, EventType eventType) {
        this.service = service;
        this.instance = instance;
        this.eventType = eventType;
    }
}
