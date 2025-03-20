package com.fbytes.llmka.service.NewsDataCheck;

import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsCheckRejectReason;

import java.util.Optional;

public interface INewsDataCheck {
    Optional<NewsCheckRejectReason> checkNewsData(String schema, EmbeddedData newsData);
    void cleanupStore(String schema);
}
