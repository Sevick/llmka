package com.fbytes.llmka.service.Maintenance.MDC;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MDCService implements IMDCService {

    @Override
    public void clearMDC() {
        ThreadContext.clearAll();   // log4j2
        //MDC.clear();                // slf4j
    }

    @Override
    public void clearMDC(String[] keys) {
        for (String key : keys) {
            ThreadContext.remove(key);   // log4j2
        }
    }

    @Override
    public void setMDC(String key, String value) {
        ThreadContext.put(key, value);
    }

    @Override
    public void setMDC(Map<String, String> contextMap) {
        ThreadContext.putAll(contextMap);
    }
}
