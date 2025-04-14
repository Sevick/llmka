package com.fbytes.llmka.service.LLMProvider;

public interface ILLMProvider {

    String askLLM(String systemPrompt, String userPrompt);
}
