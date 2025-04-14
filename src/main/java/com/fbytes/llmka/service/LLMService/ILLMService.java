package com.fbytes.llmka.service.LLMService;


import com.fbytes.llmka.service.LLMProvider.ILLMProvider;

public interface ILLMService {

    ILLMProvider findProvider(String providerName);
}
