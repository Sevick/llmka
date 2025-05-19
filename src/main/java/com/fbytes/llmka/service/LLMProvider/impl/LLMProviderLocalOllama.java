package com.fbytes.llmka.service.LLMProvider.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.LLMProvider.LLMProvider;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
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
import java.util.Optional;

@Service
@Qualifier("localOllama")
public class LLMProviderLocalOllama extends LLMProvider {
    private static final Logger logger = Logger.getLogger(LLMProviderLocalOllama.class);

    @Value("${llmka.llm_provider.ollama_onprem.model_name}")
    private String model_name;
    @Value("${llmka.llm_provider.ollama_onprem.base_url}")
    private String base_url;
    @Value("${llmka.llm_provider.ollama_onprem.timeout}")
    private Duration timeOut;

    private ChatLanguageModel model;

    public LLMProviderLocalOllama(@Autowired MeterRegistry meterRegistry) {
        super("localOllama", meterRegistry);
    }

    @PostConstruct
    public void init() {
        model = OllamaChatModel.builder()
                .baseUrl(base_url)
                .modelName(model_name)
                .responseFormat(ResponseFormat.TEXT)
                .timeout(timeOut)
                .maxRetries(1)
                .build();
    }

    @Override
    //@SemaphoreGuard(limit = 1)
    public String askLLMImpl(Optional<String> systemPrompt, String userPrompt, Optional<ResponseFormat> responseFormat) {
        List<ChatMessage> messages = new ArrayList<>();
        if (userPrompt != null && !userPrompt.isEmpty()) {
            messages.add(UserMessage.from(userPrompt));
        } else {
            throw new RuntimeException("User prompt should not be null or empty");
        }
        systemPrompt.ifPresent(systemPromptStr -> messages.add(SystemMessage.from(systemPromptStr)));


        ResponseFormat defaultResponseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.TEXT).build();


        ChatRequestParameters chatRequestParameters = ChatRequestParameters.builder()
                .responseFormat(responseFormat.orElse(defaultResponseFormat))
                .temperature(0.0d)
                .build();

        try {
            ChatResponse chatResponse = model.chat(ChatRequest.builder().messages(messages).build());
            return chatResponse.aiMessage().text();
        } catch (Exception e) {
            logger.error("Error while calling LLM: ", e.getMessage());
            throw new RuntimeException("Error while calling LLM: " + e.getMessage(), e);
        }
    }
}
