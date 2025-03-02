package com.fbytes.llmka.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class PollerConfig {
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Number of core threads
        executor.setMaxPoolSize(10); // Maximum number of threads
        executor.setQueueCapacity(25); // Queue capacity for tasks
        executor.initialize();
        return executor;
    }


    @Bean(name = "configPoller")
    public PollerMetadata configPoller() {
        return Pollers.fixedRate(60000) // Poll every 5 min
                .taskExecutor(taskExecutor()) // Use the task executor for concurrency
                .maxMessagesPerPoll(1) // Optional: limit the number of messages per poll
                .getObject();
    }
}
