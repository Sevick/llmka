package com.fbytes.llmka.service.LLMProvider;

import com.fbytes.llmka.logger.Logger;
import dev.langchain4j.model.chat.request.ResponseFormat;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Optional;

public abstract class LLMProvider implements ILLMProvider {
    private static final Logger logger = Logger.getLogger(LLMProvider.class);

    private final String name;
    private final MeterRegistry meterRegistry;
    private Counter wordsInCounter;
    private Counter wordsOutCounter;

    public LLMProvider(String name, MeterRegistry meterRegistry) {
        this.name = name;
        this.meterRegistry = meterRegistry;
        if (meterRegistry != null) {
            wordsInCounter = Counter.builder("llmka.llmprovider.localollama.wordsin.count").register(meterRegistry);
            wordsOutCounter = Counter.builder("llmka.llmprovider.localollama.wordsout.count").register(meterRegistry);
        }
    }

    abstract protected String askLLMImpl(Optional<String> systemPrompt, String userPrompt, Optional<ResponseFormat> responseFormat);

    public String getName() {
        return name;
    }

    @Override
    public String askLLM(Optional<String> systemPrompt, String userPrompt, Optional<ResponseFormat> responseFormat) {
        wordsInCounter.increment(userPrompt.split("[\\s,;:.]+").length);
        logger.debug("[{}] [{}] LLMProvider askLLM systemPrompt: {}, userPrompt: {}", getName(), Thread.currentThread().getName(), systemPrompt.orElse(""), userPrompt);
        String result = askLLMImpl(systemPrompt, userPrompt, responseFormat);
        logger.debug("[{}] [{}] LLMProvider askLLM result: {}", getName(), Thread.currentThread().getName(), result);
        wordsOutCounter.increment(result.split("[\\s,;:.]+").length);
        return result;
    }
}
