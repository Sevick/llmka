package com.fbytes.llmka.model.heraldmessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class TelegramMessage extends HeraldMessage{
    private String messageText;

    public static HeraldMessage fromString(String str) {
        return new TelegramMessage(str);
    }
}
