package com.fbytes.llmka.integration.service;

import com.fbytes.llmka.service.Maintenance.MDC.IMDCService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Service;

@Service
public class MdcClearingTaskDecorator implements TaskDecorator {
    private static final Logger logger = LogManager.getLogger(MdcClearingTaskDecorator.class);
    private final IMDCService mdcService;

    public MdcClearingTaskDecorator(@Autowired IMDCService mdcService) {
        this.mdcService = mdcService;
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } finally {
                logger.trace("Cleaning the MDC context");
                mdcService.clearMDC();
            }
        };
    }
}
