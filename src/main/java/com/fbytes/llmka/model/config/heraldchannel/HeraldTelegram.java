package com.fbytes.llmka.model.config.heraldchannel;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TELEGRAM")
public class HeraldTelegram extends Herald {
    private String bot;

    public HeraldTelegram(){
        super();
    }

    public HeraldTelegram(String id, String channel, String name, String bot) {
        super(id, "TELEGRAM", channel, name);
        this.bot = bot;
    }
}
