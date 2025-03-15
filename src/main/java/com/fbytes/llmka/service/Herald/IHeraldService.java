package com.fbytes.llmka.service.Herald;

import com.fbytes.llmka.model.heraldmessage.HeraldMessage;

public interface IHeraldService<T extends HeraldMessage> {
    void sendMessage(T msg);
}
