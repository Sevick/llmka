package com.fbytes.llmka.config.profiles.dev;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.integration.http.config.EnableIntegrationGraphController;

@Configuration
@Profile("dev")
@EnableIntegrationGraphController(path = "/igraph")
@EnableMessageHistory
public class DevProfileConfig {
}
