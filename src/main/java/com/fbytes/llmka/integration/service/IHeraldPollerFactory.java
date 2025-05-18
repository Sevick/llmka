package com.fbytes.llmka.integration.service;

import org.springframework.integration.scheduling.PollerMetadata;

public interface IHeraldPollerFactory {
    PollerMetadata createHeraldPollerService(String pollerName);
}
