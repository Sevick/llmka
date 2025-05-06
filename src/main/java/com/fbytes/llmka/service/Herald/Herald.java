package com.fbytes.llmka.service.Herald;

import com.fbytes.llmka.model.heraldmessage.HeraldMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public abstract class Herald<T extends HeraldMessage> implements IHerald<T> {
    private String name;
    private String type;

}
