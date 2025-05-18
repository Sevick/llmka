package com.fbytes.llmka.model.appevent;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
public class AppEvent {

    public enum EventType {METAHASH_COMPRESS, DB_COMPRESS, STARTUP, SHUTDOWN}

    private String service;
    private String instance;
    private EventType eventType;

    private String id;
    private long timestamp;

    private AppEvent() {
        init();
    }

    public AppEvent(String service, String instance, EventType eventType) {
        this.service = service;
        this.instance = instance;
        this.eventType = eventType;
        init();
    }

    private void init() {
        timestamp = System.currentTimeMillis();
        id = UUID.randomUUID().toString();
    }
}
