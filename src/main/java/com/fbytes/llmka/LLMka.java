package com.fbytes.llmka;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableIntegration
@EnableWebMvc
@EnableMessageHistory
@EnableIntegrationGraphController(path = "/igraph", allowedOrigins = "http://localhost:8080")
public class LLMka {
    public static void main(String[] args) {
        new SpringApplicationBuilder(LLMka.class)
                .run(args);
    }
}
