package com.fbytes.llmka.service.NewsProcessor.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.LLMProvider.ILLMProvider;
import com.fbytes.llmka.service.LLMService.ILLMService;
import com.fbytes.llmka.service.NewsProcessor.INewsProcessor;
import com.fbytes.llmka.tools.TextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Optional;

@Service
public class NewsProcessorLastSentence extends NewsProcessor implements INewsProcessor {
    private static final Logger logger = Logger.getLogger(NewsProcessorLastSentence.class);

    private final Optional<String> iscompleteSystemPrompt;
    private final Optional<String> hasfactsSystemPrompt;
    private final Optional<String> rewriteSystemPrompt;
    @Value("${llmka.lastsentence.enabled:true}")
    private Boolean serviceEnabled;
    @Value("${llmka.lastsentence.min_sentence_words_count:4}")
    private Integer minSentenceWordsCount;
    @Value("${llmka.lastsentence.iscomplete.prompt.user}")
    private String iscompleteUserPrompt;
    @Value("${llmka.lastsentence.hasfacts.prompt.user}")
    private String hasfactsUserPrompt;
    @Value("${llmka.lastsentence.rewrite.prompt.user}")
    private String rewriteUserPrompt;


    private final ILLMProvider llmProvider;


    public NewsProcessorLastSentence(@Autowired ILLMService illmService,
                                     @Value("${llmka.lastsentence.llm_provider}") String llmProviderName,
                                     @Value("${llmka.lastsentence.iscomplete.prompt.system:null}") String iscompleteSystemPrompt,
                                     @Value("${llmka.lastsentence.hasfacts.prompt.system:null}") String hasfactsSystemPrompt,
                                     @Value("${llmka.lastsentence.rewrite.prompt.system:null}") String rewriteSystemPrompt) {

        this.llmProvider = illmService.findProvider(llmProviderName);

        this.iscompleteSystemPrompt = TextUtil.stringToOptional(iscompleteSystemPrompt);
        this.hasfactsSystemPrompt = TextUtil.stringToOptional(hasfactsSystemPrompt);
        this.rewriteSystemPrompt = TextUtil.stringToOptional(rewriteSystemPrompt);
    }


    @Override
    public NewsData process(NewsData newsData) {
        if (!serviceEnabled || !newsData.getDescription().isPresent() || newsData.getDescription().get().isEmpty())
            return newsData;

        int lastSentenceIdx = TextUtil.extractLastSentenceIdx(newsData.getDescription().get());
        String lastSentence = newsData.getDescription().get().substring(lastSentenceIdx);
        logger.debug("[{}] [{}] NewsProcessorLastSentence last sentence {}. From description: {}", newsData.getDataSourceName(), newsData.getId(), lastSentence, newsData.getDescription().orElse(""));

        boolean isRewritten = false;

        int wordsCount = TextUtil.countWords(lastSentence);
        boolean isCompleteSentence = true;
        if (wordsCount < minSentenceWordsCount) {
            logger.debug("[{}] [{}] last sentence is too short: {}", newsData.getDataSourceName(), newsData.getId(), lastSentence);
            isCompleteSentence = false;
        } else {
            isCompleteSentence = checkIsCompleteSentence(lastSentence, newsData);
            logger.debug("[{}] [{}] isIncompleteSentenceStr: {}", newsData.getDataSourceName(), newsData.getId(), isCompleteSentence);
        }
        if (isCompleteSentence)
            return newsData;
        logger.debug("[{}] [{}] isIncompleteSentenceStr - incomplete sentence detected: {}", newsData.getDataSourceName(), newsData.getId(), lastSentence);


        boolean hasFacts = true;
        if (wordsCount < minSentenceWordsCount) {
            hasFacts = false;
        } else {
            String hasFactsStr =
                    llmProvider.askLLM(hasfactsSystemPrompt,
                            MessageFormat.format(hasfactsUserPrompt, newsData.getTitle(), lastSentence),
                            Optional.empty()).trim();
            logger.debug("[{}] hasFactsStr: {}", newsData.getId(), hasFactsStr);

            try {
                hasFacts = TextUtil.extractYesNo(hasFactsStr);
            } catch (Exception e) {
                // TODO: Replace with checked
                throw new RuntimeException(MessageFormat.format("[{0}] [{0}] hasFactsStr got unexpected result from {1}: {2}", newsData.getDataSourceName(), newsData.getId(), llmProvider.getName(), hasFactsStr));
            }
        }


        logger.debug("[{}] [{}] hasFactsStr - incomplete sentence contains information: {}", newsData.getDataSourceName(), newsData.getId(), hasFacts);
        String newLastSentence;
        if (!hasFacts) {
            newLastSentence = "";
        } else {
            newLastSentence =
                    llmProvider.askLLM(rewriteSystemPrompt,
                            MessageFormat.format(rewriteUserPrompt, newsData.getTitle(), lastSentence),
                            Optional.empty()).trim();
            isRewritten = true;
            if (!newLastSentence.isEmpty()) {
                newLastSentence = " " + newLastSentence;
            }
            logger.debug("[{}] [{}] newLastSentence new last sentence: {}", newsData.getDataSourceName(), newsData.getId(), newLastSentence);
        }
        String newDescr = newsData.getDescription().get().substring(0, lastSentenceIdx).trim() + newLastSentence;

        NewsData result = newsData
                .toBuilder()
                .rewritten(isRewritten)
                .description(TextUtil.stringToOptional(newDescr))
                .build();

        return result;
    }


    private boolean checkIsCompleteSentence(String src, NewsData newsData) {
        String isIncompleteSentenceStr = llmProvider.askLLM(iscompleteSystemPrompt,
                MessageFormat.format(iscompleteUserPrompt, src),
                Optional.empty()).trim();
        logger.debug("[{}] [{}] isIncompleteSentenceStr: {}", newsData.getDataSourceName(), newsData.getId(), isIncompleteSentenceStr);
        try {
            boolean isCompleteSentence = TextUtil.extractYesNo(isIncompleteSentenceStr);
            return isCompleteSentence;
        } catch (Exception e) {
            // TODO: Replace with checked
            throw new RuntimeException(MessageFormat.format("[{0}] [{2}] isIncompleteSentenceStr got unexpected result from {3}: {3}", newsData.getDataSourceName(), newsData.getId(), llmProvider.getName(), isIncompleteSentenceStr));
        }
    }
}
