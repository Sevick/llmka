package com.fbytes.llmka;

import com.fbytes.llmka.config.TelegramBotConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/*
    Profiles (config/profiles):
        dev - enables CommandService REST controller
        metrics-enabled - enables interceptor to register metrics
 */

@SpringBootApplication
@EnableIntegration
@EnableIntegrationManagement(observationPatterns = "*")
//@EnableWebMvc
@EnableConfigurationProperties(TelegramBotConfig.class)
public class LLMka {
    public static void main(String[] args) {
        new SpringApplicationBuilder(LLMka.class)
                .run(args);
    }
}
