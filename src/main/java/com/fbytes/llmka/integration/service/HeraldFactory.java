package com.fbytes.llmka.integration.service;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.config.heraldchannel.HeraldConfig;
import com.fbytes.llmka.model.config.heraldchannel.HeraldConfigTelegram;
import com.fbytes.llmka.model.heraldmessage.HeraldMessage;
import com.fbytes.llmka.service.Herald.Herald;
import com.fbytes.llmka.service.Herald.IHeraldNameService;
import com.fbytes.llmka.service.Herald.telegram.HeraldTelegram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;


@Service
public class HeraldFactory implements IHeraldFactory {
    private static final Logger logger = Logger.getLogger(HeraldFactory.class);
    private final GenericApplicationContext applicationContext;

    public HeraldFactory(@Autowired GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Herald<HeraldMessage> createHeraldService(HeraldConfig heraldConfig) {
        String beanName = IHeraldNameService.makeFullName(heraldConfig);
        logger.debug("Creating Herald bean {}", beanName);
        applicationContext.registerBean(beanName, HeraldTelegram.class, () ->
                new HeraldTelegram((HeraldConfigTelegram) heraldConfig)
        );
        logger.debug("created bean {} of type {}", beanName, heraldConfig.getClass().getSimpleName());
        return (Herald<HeraldMessage>) applicationContext.getBean(beanName);
    }
}
