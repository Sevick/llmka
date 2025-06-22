package com.fbytes.llmka.service.NewsProcessor.impl.LastSentence;

import com.fbytes.llmka.service.LLMService.ILLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
public class LastSentenceConfig {

    @Bean(name = "NewsProcessorLastSentence")
    public NewsProcessorLastSentence newsProcessorLastSentence(
            @Autowired ILLMService illmService,
            @Value("${llmka.lastsentence.llm_provider}") String llmProviderName,
            @Value("${llmka.lastsentence.iscomplete.prompt.system}") String iscompleteSystemPrompt,
            @Value("${llmka.lastsentence.iscomplete.prompt.user}") String iscompleteUserPrompt,
            @Value("${llmka.lastsentence.hasfacts.prompt.system}") String hasfactsSystemPrompt,
            @Value("${llmka.lastsentence.hasfacts.prompt.user}") String hasfactsUserPrompt,
            @Value("${llmka.lastsentence.rewrite.prompt.system}") String rewriteSystemPrompt,
            @Value("${llmka.lastsentence.rewrite.prompt.user}") String rewriteUserPrompt) {
        return new NewsProcessorLastSentence(
                illmService, llmProviderName, iscompleteSystemPrompt, iscompleteUserPrompt,
                hasfactsSystemPrompt, hasfactsUserPrompt, rewriteSystemPrompt, rewriteUserPrompt);
    }


    @Bean(name = "NewsProcessorLastSentenceBackup")
    public NewsProcessorLastSentence newsProcessorLastSentenceBackup(
            @Autowired ILLMService illmService,
            @Value("${llmka.lastsentence.llm_provider_backup:null}") String llmProviderName,
            @Value("${llmka.lastsentence.iscomplete.prompt.system}") String iscompleteSystemPrompt,
            @Value("${llmka.lastsentence.iscomplete.prompt.user}") String iscompleteUserPrompt,
            @Value("${llmka.lastsentence.hasfacts.prompt.system}") String hasfactsSystemPrompt,
            @Value("${llmka.lastsentence.hasfacts.prompt.user}") String hasfactsUserPrompt,
            @Value("${llmka.lastsentence.rewrite.prompt.system}") String rewriteSystemPrompt,
            @Value("${llmka.lastsentence.rewrite.prompt.user}") String rewriteUserPrompt) {
        return new NewsProcessorLastSentence(
                illmService, llmProviderName, iscompleteSystemPrompt, iscompleteUserPrompt,
                hasfactsSystemPrompt, hasfactsUserPrompt, rewriteSystemPrompt, rewriteUserPrompt);
    }
}
