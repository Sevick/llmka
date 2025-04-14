package com.fbytes.llmka.service.LLMProvider.impl;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Qualifier("localOllama")
public class LLMProviderLocalOllama extends LLMProvider {

    @Value("${llmka.llm_provider.ollama_onprem.model_name}")
    private String model_name;
    @Value("${llmka.llm_provider.ollama_onprem.base_url}")
    private String base_url;
    @Value("${llmka.llm_provider.ollama_onprem.timeout}")
    private Duration timeOut;


    private ChatLanguageModel model;

    public LLMProviderLocalOllama(@Autowired MeterRegistry meterRegistry) {
        super(meterRegistry);
    }

    @PostConstruct
    public void init() {
        model = OllamaChatModel.builder()
                .baseUrl(base_url)
                .modelName(model_name)
                .responseFormat(ResponseFormat.TEXT)
                .timeout(timeOut)
                .build();

    }

    @Override
    public String askLLMImpl(String systemPrompt, String userPrompt) {
        List<ChatMessage> messages = new ArrayList<>();
        if (userPrompt != null && !userPrompt.isEmpty()) {
            messages.add(UserMessage.from(userPrompt));
        } else {
            throw new RuntimeException("User prompt should not be null or empty");
        }
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(SystemMessage.from(systemPrompt));
        }
        ;

        ChatResponse chatResponse = model.chat(ChatRequest.builder().messages(messages).build());
        return chatResponse.aiMessage().text();
    }
}
