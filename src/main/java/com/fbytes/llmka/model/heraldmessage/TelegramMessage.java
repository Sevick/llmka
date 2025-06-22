package com.fbytes.llmka.model.heraldmessage;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.tools.TextUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TelegramMessage extends HeraldMessage {
    private String messageText;

    public static TelegramMessage fromString(String str) {
        return new TelegramMessage(str);
    }
}
