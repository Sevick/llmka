package com.fbytes.llmka.service.NewsCheck.impl;

import com.fbytes.llmka.config.profiles.merics.ParamTimedMetric;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.LLMProvider.ILLMProvider;
import com.fbytes.llmka.service.LLMService.ILLMService;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
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

    @Value("${llmka.newscheck.adcheck.prompt.system}")
    private String systemPrompt;
    @Value("${llmka.newscheck.adcheck.prompt.user}")
    private String userPrompt;
    @Value("${llmka.newscheck.adcheck.timeout}")
    private Duration timeOut;
    @Value("${llmka.newscheck.adcheck.llm_provider}")
    private String llmProviderName;


    public NewsCheckAd(@Autowired ILLMService illmService,
                       @Value("${llmka.newscheck.adcheck.llm_provider}") String llmProviderName) {
        this.llmProvider = illmService.findProvider(llmProviderName);
    }


    @Override
    @ParamTimedMetric(key = "schema")
    @NewSpan(name = "checkad-span")
    public Optional<RejectReason> checkNews(String schema, NewsData newsData) {
        String response = llmProvider.askLLM(systemPrompt, MessageFormat.format(userPrompt, newsData.toText()));
        if (response.toLowerCase().startsWith("yes")) {
            logger.debug("COMMERCIAL. Message: {}", newsData);
            return Optional.of(new RejectReason(RejectReason.REASON.COMMERCIAL));
        } else {
            return Optional.empty();
        }
    }
}
