package com.fbytes.llmka.service;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class UptimeService {

    private Instant startTime;

    @EventListener
    public void onApplicationStart(ApplicationStartedEvent event) {
        this.startTime = Instant.now();
    }

    public Duration getUptime() {
        return Duration.between(startTime, Instant.now());
    }
}
