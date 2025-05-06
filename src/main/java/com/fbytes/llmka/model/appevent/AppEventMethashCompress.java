package com.fbytes.llmka.model.appevent;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class AppEventMethashCompress extends AppEvent {

    public AppEventMethashCompress(String srv, String schema) {
        super(srv, schema, EventType.METAHASH_COMPRESS);
    }
}
