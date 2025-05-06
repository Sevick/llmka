package com.fbytes.llmka.integration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;

@Configuration
public class PollerConfig {

    @Value("${llmka.threads.poller_prefix}")
    private String pollerPrefix;


    @Bean(name = "newsSourceTaskExecutor")
    public TaskExecutor newsSourceTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);  // LinkedBlocking
        executor.setThreadNamePrefix(pollerPrefix + "NewsSource-");
        //executor.setTaskDecorator(runnable -> ContextSnapshot.captureAll().wrap(runnable));
        executor.initialize();
        return executor;
    }

    @Bean(name = "pubsubExecutor")
    public TaskExecutor pubsubExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(5);  // LinkedBlocking
        executor.setThreadNamePrefix(pollerPrefix + "PubSubExecutor-");
        //executor.setTaskDecorator(runnable -> ContextSnapshot.captureAll().wrap(runnable));
        executor.initialize();
        return executor;
    }

    @Bean(name = "heraldTaskExecutor")
    public TaskExecutor heraldTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);  // LinkedBlocking
        executor.setThreadNamePrefix(pollerPrefix + "HeraldTaskExecutor-");
        //executor.setTaskDecorator(runnable -> ContextSnapshot.captureAll().wrap(runnable));
        executor.initialize();
        return executor;
    }

//    @Bean(name = "telegramdTaskExecutor")
//    public TaskExecutor telegramdTaskExecutor() {
//        //SyncTaskExecutor executor = new SyncTaskExecutor();
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(3);
//        executor.setMaxPoolSize(10);
//        executor.setQueueCapacity(200);  // LinkedBlocking
//        executor.setThreadNamePrefix("telegramdTaskExecutor-");
//        //executor.setTaskDecorator(runnable -> ContextSnapshot.captureAll().wrap(runnable));
//        executor.initialize();
//        return executor;
//    }


    @Bean(name = "newsSourcePoller")
    public PollerMetadata newsSourcePoller() {
        return Pollers.fixedDelay(Duration.ofSeconds(30), Duration.ofSeconds(10))
                .taskExecutor(newsSourceTaskExecutor())
                .maxMessagesPerPoll(1)
                .getObject();
    }

    @Bean(name = "heraldChannelPoller")
    public PollerMetadata heraldChannelPoller() {
        return Pollers.fixedDelay(Duration.ofSeconds(30))
                .taskExecutor(heraldTaskExecutor())
                .maxMessagesPerPoll(1)
                .getObject();
    }
}
