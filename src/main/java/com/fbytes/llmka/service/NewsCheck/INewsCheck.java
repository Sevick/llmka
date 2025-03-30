package com.fbytes.llmka.service.NewsCheck;

import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsCheckRejectReason;

import java.util.Optional;

public interface INewsCheck {
    Optional<NewsCheckRejectReason> checkNews(String schema, EmbeddedData newsData);
}
