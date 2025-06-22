package com.fbytes.llmka.service.NewsProcessor.impl.LastSentence;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.LLMProvider.ILLMProvider;
import com.fbytes.llmka.service.LLMService.ILLMService;
import com.fbytes.llmka.service.NewsProcessor.INewsProcessor;
import com.fbytes.llmka.service.NewsProcessor.impl.NewsProcessor;
import com.fbytes.llmka.tools.TextUtil;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Optional;

//@Service
//@Qualifier("NewsProcessorLastSentence")
public class NewsProcessorLastSentence extends NewsProcessor implements INewsProcessor {
    private static final Logger logger = Logger.getLogger(NewsProcessorLastSentence.class);

    @Value("${llmka.lastsentence.enabled:true}")
    private Boolean serviceEnabled;
    @Value("${llmka.lastsentence.min_sentence_words_count:4}")
    private Integer minSentenceWordsCount;

    private final String iscompleteUserPrompt;
    private final String hasfactsUserPrompt;
    private final String rewriteUserPrompt;
    private final Optional<String> iscompleteSystemPrompt;
    private final Optional<String> hasfactsSystemPrompt;
    private final Optional<String> rewriteSystemPrompt;


    private final ILLMProvider llmProvider;


    public NewsProcessorLastSentence(ILLMService illmService,
                                       String llmProviderName,
                                       String iscompleteSystemPrompt,
                                       String iscompleteUserPrompt,
                                       String hasfactsSystemPrompt,
                                       String hasfactsUserPrompt,
                                       String rewriteSystemPrompt,
                                       String rewriteUserPrompt
    ) {

        this.llmProvider = illmService.findProvider(llmProviderName);

        this.iscompleteSystemPrompt = TextUtil.stringToOptional(iscompleteSystemPrompt);
        this.hasfactsSystemPrompt = TextUtil.stringToOptional(hasfactsSystemPrompt);
        this.rewriteSystemPrompt = TextUtil.stringToOptional(rewriteSystemPrompt);

        this.iscompleteUserPrompt = iscompleteUserPrompt;
        this.hasfactsUserPrompt = hasfactsUserPrompt;
        this.rewriteUserPrompt = rewriteUserPrompt;
    }


    @Override
    @Timed(value = "llmka.lastsentence.time", description = "time to process last sentence")
    public NewsData process(NewsData newsData) {
        if (!serviceEnabled || !newsData.getDescription().isPresent() || newsData.getDescription().get().isEmpty())
            return newsData;

        int lastSentenceIdx = TextUtil.extractLastSentenceIdx(newsData.getDescription().get());
        String lastSentence = newsData.getDescription().get().substring(lastSentenceIdx);
        logger.debug("last sentence {}. From description: {}", lastSentence, newsData.getDescription().orElse(""));

        String newDescr;
        boolean isRewritten = false;

        int wordsCount = TextUtil.countWords(lastSentence);
        boolean isCompleteSentence = true;
        if (wordsCount < minSentenceWordsCount) {
            logger.debug("last sentence is too short: {} words. Considered incomplete.", wordsCount);

            isCompleteSentence = false;
        } else {
            isCompleteSentence = !checkIsCompleteSentence(lastSentence);
            logger.debug("isIncompleteSentenceStr (llm): {}", isCompleteSentence);
        }
        if (isCompleteSentence)
            return newsData;

        if (wordsCount < minSentenceWordsCount) {
            newDescr = newsData.getDescription().get().substring(0, lastSentenceIdx).trim();
        } else {
            newDescr =
                    llmProvider.askLLM(rewriteSystemPrompt,
                            MessageFormat.format(rewriteUserPrompt, newsData.getTitle(), newsData.getDescription().get()),
                            Optional.empty()).trim();
        }

        NewsData result = newsData
                .toBuilder()
                .rewritten(isRewritten)
                .description(TextUtil.stringToOptional(newDescr))
                .build();
        return result;
    }


    private boolean checkIsCompleteSentence(String src) {
        String isIncompleteSentenceStr = llmProvider.askLLM(iscompleteSystemPrompt,
                MessageFormat.format(iscompleteUserPrompt, src),
                Optional.empty()).trim();
        logger.debug("isIncompleteSentenceStr: {}", isIncompleteSentenceStr);
        try {
            boolean isCompleteSentence = TextUtil.extractYesNo(isIncompleteSentenceStr);
            return isCompleteSentence;
        } catch (Exception e) {
            // TODO: Replace with checked
            throw new RuntimeException(MessageFormat.format("isIncompleteSentenceStr got unexpected result from {0}: {1}", llmProvider.getName(), isIncompleteSentenceStr));
        }
    }
}
