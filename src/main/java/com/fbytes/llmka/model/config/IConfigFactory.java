package com.fbytes.llmka.model.config;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IConfigFactory<T> {
    T getParams(String json) throws JsonProcessingException;
}
