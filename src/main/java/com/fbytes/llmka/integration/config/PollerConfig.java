package com.fbytes.llmka.integration.config;

import com.fbytes.llmka.service.Maintenance.MDC.IMDCService;
import com.fbytes.llmka.integration.service.MdcClearingTaskDecorator;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Value("${llmka.newssource.poll_interval}")
    private Duration newsSourcePollInterval;
    @Value("${llmka.newssource.poll_delay}")
    private Duration newsSourcePollDelay;

    @Autowired
    private MdcClearingTaskDecorator mdcClearingTaskDecorator;
    @Autowired
    private IMDCService mdcService;

    @Bean(name = "newsSourceTaskExecutor")
    public TaskExecutor newsSourceTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);  // LinkedBlocking
        executor.setThreadNamePrefix(pollerPrefix + "NewsSource-");
        executor.setTaskDecorator(mdcClearingTaskDecorator);
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
        executor.setTaskDecorator(mdcClearingTaskDecorator);
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
        executor.setTaskDecorator(mdcClearingTaskDecorator);
        //executor.setTaskDecorator(runnable -> ContextSnapshot.captureAll().wrap(runnable));
        executor.initialize();
        return executor;
    }

    @Bean(name = "newsSourcePoller")
    public PollerMetadata newsSourcePoller() {
        return Pollers.fixedDelay(newsSourcePollInterval, newsSourcePollDelay)
                .taskExecutor(newsSourceTaskExecutor())
                .maxMessagesPerPoll(1)
                .getObject();
    }
}
