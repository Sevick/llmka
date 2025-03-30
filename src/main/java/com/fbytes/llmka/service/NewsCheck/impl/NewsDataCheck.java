package com.fbytes.llmka.service.NewsCheck.impl;

import com.fbytes.llmka.config.profiles.merics.ParamTimedMetric;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsCheckRejectReason;
import com.fbytes.llmka.service.EmbeddedStore.IEmbeddedStoreService;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import dev.langchain4j.rag.content.Content;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class NewsDataCheck implements INewsCheck {
    private static final Logger logger = Logger.getLogger(NewsDataCheck.class);

    @Value("${llmka.datacheck.metahash_size_limit:64}")
    private Integer metaHashSizeLimit;
    @Value("${llmka.datacheck.metahash_size_core:32}")
    private Integer metaHashSizeCore;
    @Value("#{T(Float).parseFloat('${llmka.datacheck.score_limit}')}")
    private Float scoreLimit;

    @Autowired
    private IEmbeddedStoreService embeddingStoreService;
    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    NewsMetaCheck newsMetaCheckService;


    @Override
    //@Timed(value = "llmka.newsdatacheck.time", description = "time to check news for duplicates", percentiles = {0.5, 0.9})
    @ParamTimedMetric(key = "schema")
    public Optional<NewsCheckRejectReason> checkNews(String schema, EmbeddedData embeddedData) {
        Optional<NewsCheckRejectReason> newsCheckRejectReason = newsMetaCheckService.checkNews(schema, embeddedData);
        if (!newsCheckRejectReason.isEmpty())
            return newsCheckRejectReason;
        Optional<List<Content>> result = embeddingStoreService.checkAndStore(schema, embeddedData.getSegments(), embeddedData.getEmbeddings(), scoreLimit);
        if (!result.isEmpty())
            return Optional.of(new NewsCheckRejectReason(NewsCheckRejectReason.REASON.CLOSE_MATCH, result.get().get(0).textSegment().text()));
        logger.debug("News check passed");
        return Optional.empty();
    }


    // remove all IDes, that are not in metaHash
    @Timed(value = "llmka.newsdatacheck.cleanupstore.time", description = "time to cleanup the store", percentiles = {0.5, 0.9})
    public void cleanupStore(String schema, Set<String> idsSet) {
        logger.info("cleanupStore: {}", schema);
        embeddingStoreService.removeOtherIDes(schema, idsSet);
    }
}
