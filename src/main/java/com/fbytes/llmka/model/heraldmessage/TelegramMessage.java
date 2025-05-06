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

    public static TelegramMessage fromNewsData(NewsData newsData) {
        return new TelegramMessage(String.format("*%s* %s\t([%s](%s))%s",
                TextUtil.cleanMarkdown(newsData.getTitle()),
                TextUtil.checkAddLastDot(TextUtil.cleanMarkdown(newsData.getDescription().orElse(""))),
                newsData.getDataSourceName(),
                newsData.getLink(),
                newsData.isRewritten() ? "\\*" : ""));
    }
}
