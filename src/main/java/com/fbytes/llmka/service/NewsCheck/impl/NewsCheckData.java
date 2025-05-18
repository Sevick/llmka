package com.fbytes.llmka.service.NewsCheck.impl;

import com.fbytes.llmka.config.profiles.metrics.ParamTimedMetric;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.EmbeddedStore.IEmbeddedStoreService;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import dev.langchain4j.rag.content.Content;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Qualifier("newsCheckData")
public class NewsCheckData implements INewsCheck {
    private static final Logger logger = Logger.getLogger(NewsCheckData.class);

    @Value("${llmka.newscheck.datacheck.enabled:true}")
    private Boolean serviceEnabled;
    @Value("#{T(Float).parseFloat('${llmka.newscheck.datacheck.score_limit}')}")
    private Float scoreLimit;

    @Autowired
    private IEmbeddingService embeddingService;
    @Autowired
    private IEmbeddedStoreService embeddingStoreService;

    @Override
    @ParamTimedMetric(key = "schema")
    public Optional<RejectReason> checkNews(String schema, NewsData newsData) {
        if (!serviceEnabled)
            return Optional.empty();

        EmbeddedData embeddedData = embeddingService.embedNewsData(newsData);
        logger.debug("dataSimilarity check");
        Optional<List<Content>> result = embeddingStoreService.checkAndStore(schema, embeddedData.getSegments(), embeddedData.getEmbeddings(), scoreLimit);
        if (!result.isEmpty()) {
            logger.trace("NewsCheckData filtered: {}  CloseMatch: {}", newsData.toText(), Optional.of(result.get().get(0).textSegment().text()));
            return Optional.of(new RejectReason(RejectReason.REASON.CLOSE_MATCH, Optional.of(result.get().get(0).textSegment().text())));
        }
        logger.trace("news check passed");
        return Optional.empty();
    }
}
