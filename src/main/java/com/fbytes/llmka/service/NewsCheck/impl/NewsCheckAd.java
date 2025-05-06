package com.fbytes.llmka.service.NewsCheck.impl;

import com.fbytes.llmka.config.profiles.metrics.ParamTimedMetric;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.LLMProvider.ILLMProvider;
import com.fbytes.llmka.service.LLMService.ILLMService;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import com.fbytes.llmka.tools.TextUtil;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Optional;

@Service
public class NewsCheckAd implements INewsCheck {
    private static final Logger logger = Logger.getLogger(NewsCheckAd.class);
    private final ILLMProvider llmProvider;
    private final Optional<String> systemPrompt;

    @Value("${llmka.newscheck.adcheck.enabled:true}")
    private Boolean serviceEnabled;

    @Value("${llmka.newscheck.adcheck.prompt.user}")
    private String userPrompt;
    @Value("${llmka.newscheck.adcheck.timeout}")
    private Duration timeOut;


    public NewsCheckAd(@Autowired ILLMService illmService,
                       @Value("${llmka.newscheck.adcheck.llm_provider}") String llmProviderName,
                       @Value("${llmka.newscheck.adcheck.prompt.system}") String systemPrompt) {
        this.llmProvider = illmService.findProvider(llmProviderName);

        if (systemPrompt==null || systemPrompt.isEmpty())
            this.systemPrompt = Optional.empty();
        else
            this.systemPrompt = Optional.of(systemPrompt);
    }


    @Override
    @ParamTimedMetric(key = "schema")
    public Optional<RejectReason> checkNews(String schema, NewsData newsData) {
        if (!serviceEnabled)
            return Optional.empty();

        logger.trace("[{}] NewsCheckAd. Checking news data: {}", schema, newsData.toText());
        String response = llmProvider.askLLM(systemPrompt, MessageFormat.format(userPrompt, newsData.toText()), Optional.empty());
        logger.trace("NewsCheckAd. LLM response: {}", response);
        boolean answer = false;
        try {
            answer = TextUtil.extractYesNo(response);
        } catch (TextUtil.TextParsingException e) {
            logger.warn("[{}] NewsCheckAd. Unable to parse LLM response: {}\nNewsData: {}", schema, response, newsData);
            // assume non-commercial if service is not available
        }

        if (answer) {
            logger.debug("[{}] NewsCheckAd filtered : {}\nLLM respons: {}", schema, newsData.toText(), response);
            return Optional.of(new RejectReason(RejectReason.REASON.COMMERCIAL, Optional.of(response)));
        } else {
            return Optional.empty();
        }
    }
}
