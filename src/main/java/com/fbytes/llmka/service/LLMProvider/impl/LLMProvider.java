package com.fbytes.llmka.service.LLMProvider.impl;

import com.fbytes.llmka.service.LLMProvider.ILLMProvider;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.text.BreakIterator;
import java.util.Locale;

public abstract class LLMProvider implements ILLMProvider//, ILLMStatsProvider
{

    BreakIterator breakIterator = BreakIterator.getWordInstance(Locale.ENGLISH);    //TODO: Set appropriate locale

//    private final AtomicLong statWordsIn = new AtomicLong();
//    private final AtomicLong statWordsOut = new AtomicLong();

    private final MeterRegistry meterRegistry;
    private Counter wordsInCounter;
    private Counter wordsOutCounter;

    public LLMProvider(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        if (meterRegistry != null) {
            wordsInCounter = Counter.builder("llmka.llmprovider.localollama.wordsin.count").register(meterRegistry);
            wordsOutCounter = Counter.builder("llmka.llmprovider.localollama.wordsout.count").register(meterRegistry);
        }
    }

    abstract protected String askLLMImpl(String systemPrompt, String userPrompt);

    @Override
    public String askLLM(String systemPrompt, String userPrompt) {
        wordsInCounter.increment(userPrompt.split("[\\s,;:.]+").length);
        //statWordsIn.addAndGet(userPrompt.split("[\\s,;:.]+").length);
        wordsInCounter.increment();
        String result = askLLMImpl(systemPrompt, userPrompt);
        wordsOutCounter.increment(result.split("[\\s,;:.]+").length);
        //statWordsOut.addAndGet(result.split("[\\s,;:.]+").length);
        return result;
    }

//    @Override
//    public long fetchStatWordsIn() {
//        return statWordsIn.get();
//    }
//
//    @Override
//    public long fetchStatWordsOut() {
//        return statWordsOut.get();
//    }
}
