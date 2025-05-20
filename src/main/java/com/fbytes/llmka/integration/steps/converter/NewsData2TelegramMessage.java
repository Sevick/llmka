package com.fbytes.llmka.integration.steps.converter;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.heraldmessage.TelegramMessage;
import org.springframework.core.convert.converter.Converter;
import org.springframework.integration.config.IntegrationConverter;
import org.springframework.stereotype.Service;

@Service
@IntegrationConverter
public class NewsData2TelegramMessage implements Converter<NewsData, TelegramMessage> {

    @Override
    public TelegramMessage convert(NewsData source) {
        return TelegramMessage.fromNewsData(source);
    }
}