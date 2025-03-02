package com.fbytes.llmka.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsCheckRejectReason {
    public enum REASON { META_DUPLICATION, CLOSE_MATCH };
    REASON reason;
    String explain;
}
