package com.fbytes.llmka.service.NewsDataCheck;

import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsCheckRejectReason;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public interface INewsDataCheck {
    Pair<Boolean, Optional<NewsCheckRejectReason>> checkNewsData(EmbeddedData newsData);
}
