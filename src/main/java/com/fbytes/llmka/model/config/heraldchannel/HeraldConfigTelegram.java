package com.fbytes.llmka.model.config.heraldchannel;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TELEGRAM")
public class HeraldConfigTelegram extends HeraldConfig {
    private String bot;
    private String refurl;

    public HeraldConfigTelegram(){
        super();
    }

    public HeraldConfigTelegram(String id, String channel, String name, String bot) {
        super(id, "TELEGRAM", channel, name);
        this.bot = bot;
    }
}
