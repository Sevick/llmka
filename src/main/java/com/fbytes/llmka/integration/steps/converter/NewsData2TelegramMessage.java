package com.fbytes.llmka.integration.steps.converter;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.heraldmessage.TelegramMessage;
import com.fbytes.llmka.tools.TextUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.integration.config.IntegrationConverter;
import org.springframework.stereotype.Service;

@Service
@IntegrationConverter
public class NewsData2TelegramMessage implements Converter<NewsData, TelegramMessage> {

    @Value("${llmka.herald.telegram.message_format}")
    private String messageFormat;

    @Override
    public TelegramMessage convert(NewsData newsData) {
        return new TelegramMessage(String.format(messageFormat,
                TextUtil.cleanMarkdown(newsData.getTitle()),
                TextUtil.checkAddLastDot(TextUtil.cleanMarkdown(newsData.getDescription().orElse(""))),
                newsData.getDataSourceName(),
                newsData.getLink(),
                newsData.isRewritten() ? "\\*" : ""));
    }
}