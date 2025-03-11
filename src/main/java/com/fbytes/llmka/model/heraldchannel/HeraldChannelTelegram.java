package com.fbytes.llmka.model.heraldchannel;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TELEGRAM")
public class HeraldChannelTelegram extends HeraldChannel {
    private String bot;

    public HeraldChannelTelegram(){
        super();
    }

    public HeraldChannelTelegram(String id, String type, String name, String bot) {
        super(id, type, name);
        this.bot = bot;
    }
}
