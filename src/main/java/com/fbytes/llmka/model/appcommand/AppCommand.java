package com.fbytes.llmka.model.appcommand;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
public class AppCommand {
    public enum CommandType {COMPRESS}

    @Builder.Default
    private String id = UUID.randomUUID().toString();
    @Builder.Default
    private long timestamp = System.currentTimeMillis();
}
