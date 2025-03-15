package com.fbytes.llmka.model;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IConfigFactory<T> {
    T getParams(String json) throws JsonProcessingException;
}
