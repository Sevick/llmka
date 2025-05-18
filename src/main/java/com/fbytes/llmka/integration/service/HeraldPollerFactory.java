package com.fbytes.llmka.integration.service;

import com.fbytes.llmka.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class HeraldPollerFactory implements IHeraldPollerFactory {
    private static final Logger logger = Logger.getLogger(HeraldPollerFactory.class);
    private final GenericApplicationContext applicationContext;

    @Value("${llmka.threads.poller_prefix}")
    private String pollerPrefix;
    @Value("${llmka.herald.telegram.poll_delay}")
    private Duration telegramPollDelay;

    public HeraldPollerFactory(@Autowired GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Override
    public PollerMetadata createHeraldPollerService(String pollerName) {
        logger.debug("Creating HeraldPoller bean for poller: {}", pollerName);
        String beanName = pollerPrefix + pollerName;
        applicationContext.registerBean(beanName, PollerMetadata.class, () -> {
                    TaskExecutor executor = new SyncTaskExecutor();
//                    executor.setCorePoolSize(1);
//                    executor.setMaxPoolSize(1);
//                    executor.setQueueCapacity(0);
//                    executor.setThreadNamePrefix(pollerPrefix + pollerName + "-");
//                    executor.initialize();
                    PollerMetadata poller = Pollers.fixedDelay(telegramPollDelay)
                            .taskExecutor(executor)
                            .maxMessagesPerPoll(1)
                            .getObject();
                    return poller;
                }
        );
        logger.debug("created bean {}", beanName);
        return (PollerMetadata) applicationContext.getBean(beanName);
    }
}
