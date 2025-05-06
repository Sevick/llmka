package com.fbytes.llmka.service.NewsProcessor.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.NewsProcessor.INewsProcessor;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NewsProcessorTailTrim extends NewsProcessor implements INewsProcessor {
    private static final Logger logger = Logger.getLogger(NewsProcessorTailTrim.class);

    @Value("${llmka.tailtrim.enabled:true}")
    private Boolean serviceEnabled;

    @Override
    @Timed(value = "llmka.tailtrimmer.time", description = "time to trim the tail")
    @NewSpan(name = "tailtrim-span")
    public NewsData process(NewsData newsData) {
        if (!serviceEnabled || newsData.getDescription().get().isEmpty())
            return newsData;

        logger.debug("[{}] tailTrim processing", newsData.getId());
        return newsData;
    }
}
