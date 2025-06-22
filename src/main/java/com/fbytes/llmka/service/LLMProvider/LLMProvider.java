package com.fbytes.llmka.service.LLMProvider;

import com.fbytes.llmka.logger.Logger;
import dev.langchain4j.model.chat.request.ResponseFormat;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Optional;

public abstract class LLMProvider implements ILLMProvider {
    private static final Logger logger = Logger.getLogger(LLMProvider.class);

    private final String name;
    private final Optional<MeterRegistry> meterRegistry;
    private Optional<Counter> wordsInCounter;
    private Optional<Counter> wordsOutCounter;

    public LLMProvider(String name, MeterRegistry meterRegistry) {
        this.name = name;
        this.meterRegistry = Optional.of(meterRegistry);
        if (!this.meterRegistry.isEmpty()) {
            wordsInCounter = Optional.of(Counter.builder(String.format("llmka.llmprovider.%s.wordsin.count", name)).register(meterRegistry));
            wordsOutCounter = Optional.of(Counter.builder(String.format("llmka.llmprovider.%s.wordsout.count", name)).register(meterRegistry));
        } else {
            wordsInCounter = Optional.empty();
            wordsOutCounter = Optional.empty();
        }
    }

    abstract protected String askLLMImpl(Optional<String> systemPrompt, String userPrompt, Optional<ResponseFormat> responseFormat);

    public String getName() {
        return name;
    }

    @Override
    public String askLLM(Optional<String> systemPrompt, String userPrompt, Optional<ResponseFormat> responseFormat) {
        wordsInCounter.ifPresent(cnt -> cnt.increment(userPrompt.split("[\\s,;:.]+").length));
        logger.debug("[{}] [{}] LLMProvider askLLM systemPrompt: {}, userPrompt: {}", getName(), Thread.currentThread().getName(), systemPrompt.orElse(""), userPrompt);
        String result = askLLMImpl(systemPrompt, userPrompt, responseFormat);
        logger.debug("[{}] [{}] LLMProvider askLLM result: {}", getName(), Thread.currentThread().getName(), result);
        wordsOutCounter.ifPresent(cnt -> cnt.increment(result.split("[\\s,;:.]+").length));
        return result;
    }
}
