package com.fbytes.llmka.service.LLMService;

import com.fbytes.llmka.service.LLMProvider.ILLMProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LLMService implements ILLMService {

    @Autowired
    Map<String, ILLMProvider> providerMap;

    @Override
    public ILLMProvider findProvider(String providerName) {
        return providerMap.get("LLMProvider" + providerName);
    }
}
