package com.fbytes.llmka.integration.service;

import com.fbytes.llmka.model.config.heraldchannel.HeraldConfig;
import com.fbytes.llmka.model.heraldmessage.HeraldMessage;
import com.fbytes.llmka.service.Herald.Herald;

public interface IHeraldFactory {
    Herald<HeraldMessage> createHeraldService(HeraldConfig heraldConfig);
}
