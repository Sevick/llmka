package com.fbytes.llmka.service;

import java.util.Set;

public interface INewsIDStore {
    Set<String> fetchIDList(String schema);
}
