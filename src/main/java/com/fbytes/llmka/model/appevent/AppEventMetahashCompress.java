package com.fbytes.llmka.model.appevent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper=true)
public class AppEventMetahashCompress extends AppEvent {

    public AppEventMetahashCompress(String srv, String schema) {
        super(srv, schema, EventType.METAHASH_COMPRESS);
    }
}
