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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@Qualifier("localPhi4")
public class LLMProviderLocalPhi4 extends LLMProvider {

    @Value("${llmka.llm_provider.phi4_onprem.model_name}")
    private String model_name;
    @Value("${llmka.llm_provider.phi4_onprem.base_url}")
    private String base_url;
    @Value("${llmka.llm_provider.phi4_onprem.timeout}")
    private Duration timeOut;

    public LLMProviderLocalPhi4(@Autowired MeterRegistry meterRegistry) {
        super(meterRegistry);
    }

    @Override
    public String askLLMImpl(String systemPrompt, String userPrompt) {
        List<ChatMessage> messages = List.of(
                SystemMessage.from(systemPrompt),
                UserMessage.from(userPrompt)
        );
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(base_url)
                .modelName(model_name)
                .responseFormat(ResponseFormat.TEXT)
                .timeout(timeOut)
                .build();
        ChatResponse chatResponse = model.chat(ChatRequest.builder().messages(messages).build());
        return chatResponse.aiMessage().text();
    }
}
