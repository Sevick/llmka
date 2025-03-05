package com.fbytes.llmka.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class PollerConfig {
    @Bean
    public TaskExecutor configTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);  // LinkedBlocking
        executor.setThreadNamePrefix("ConfigPoller-");
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskExecutor heraldTaskExecutor() {
        SyncTaskExecutor executor = new SyncTaskExecutor();
        return executor;
    }


    @Bean(name = "configPoller")
    public PollerMetadata configPoller() {
        return Pollers.fixedDelay(Duration.of(2, ChronoUnit.MINUTES))
                .taskExecutor(configTaskExecutor())
                .maxMessagesPerPoll(1)
                .getObject();
    }


    @Bean(name = "telegramPoller")
    public PollerMetadata telegramPoller() {
        return Pollers.fixedDelay(Duration.of(1, ChronoUnit.SECONDS))
                .taskExecutor(heraldTaskExecutor())
                .maxMessagesPerPoll(1)
                .getObject();
    }
}
