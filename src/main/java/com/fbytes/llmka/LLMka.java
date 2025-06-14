package com.fbytes.llmka;

import com.fbytes.llmka.config.TelegramBotConfig;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;

/*
    Profiles (config/profiles):
        dev - enables CommandService REST controller
        metrics-enabled - enables interceptor to register metrics
 */

@SpringBootApplication
@EnableIntegration
@EnableIntegrationManagement(defaultLoggingEnabled = "true", observationPatterns = "*")
//@EnableWebMvc
@EnableConfigurationProperties(TelegramBotConfig.class)
@EnableAspectJAutoProxy
public class LLMka {
    private static final Logger logger = LogManager.getLogger(LLMka.class);

    public static void main(String[] args) {
        new SpringApplicationBuilder(LLMka.class)
                .run(args);
    }
}
