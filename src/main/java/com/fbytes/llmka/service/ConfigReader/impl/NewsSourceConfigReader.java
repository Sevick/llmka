package com.fbytes.llmka.service.ConfigReader.impl;

import com.fbytes.llmka.model.config.newssource.NewsSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NewsSourceConfigReader extends ConfigReader<NewsSource> {

    public NewsSourceConfigReader(@Value("${llmka.config.ignore_invalid_config:true}") Boolean ignoreInvalidConfig) {
        super(ignoreInvalidConfig);
    }
};