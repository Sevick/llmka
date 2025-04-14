package com.fbytes.llmka.service.BriefMaker.impl;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.BriefMaker.IBriefMaker;
import com.fbytes.llmka.service.LLMProvider.ILLMProvider;
import com.fbytes.llmka.service.LLMService.ILLMService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Optional;

@Service
public class BriefMaker extends BriefMakerBase implements IBriefMaker {
    @Value("${llmka.brief.prompt.system}")
    private String systemPrompt;
    @Value("${llmka.brief.prompt.user}")
    private String userPrompt;
    @Value("${llmka.brief.description_size_limit}")
    private Integer descriptionSizeLimit;
    @Value("${llmka.brief.timeout}")
    private Duration timeOut;

    private final ILLMProvider llmProvider;


    public BriefMaker(@Autowired ILLMService illmService,
                      @Value("${llmka.brief.llm_provider}") String llmProviderName) {
        this.llmProvider = illmService.findProvider(llmProviderName);
    }


    @Override
    @Timed(value = "llmka.briefmaker.time", description = "time to write short description")
    @NewSpan(name = "briefmaker-span")
    public NewsData makeBrief(NewsData newsData) {
        if (newsData.getDescription().orElse("").length() <= descriptionSizeLimit)
            return newsData;

        String shortDescription = llmProvider.askLLM(systemPrompt, MessageFormat.format(userPrompt, newsData.getDescription().orElse("")));
        NewsData result = newsData.toBuilder().build();
        result.setDescription(Optional.of(shortDescription));
        result.setRewritten(true);
        return result;
    }
}
