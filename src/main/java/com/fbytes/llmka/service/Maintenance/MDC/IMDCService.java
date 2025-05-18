package com.fbytes.llmka.service.Maintenance.MDC;

import java.util.Map;

public interface IMDCService {
    void clearMDC();

    void clearMDC(String[] keys);

    void setMDC(String key, String value);

    void setMDC(Map<String, String> contextMap);
}
