package com.fbytes.llmka.service.LLMProvider;

import dev.langchain4j.model.chat.request.ResponseFormat;

import java.util.Optional;

public interface ILLMProvider {

    String askLLM(Optional<String> systemPrompt, String userPrompt, Optional<ResponseFormat> responseFormat);
    String getName();

    class LLMProviderException extends Exception {
        public LLMProviderException(String message) {
            super(message);
        }
        public LLMProviderException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
