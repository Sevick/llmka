package com.fbytes.llmka.service.EmbeddingStore.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.EmbeddingStore.IEmbeddedStoreService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class EmbeddedStoreService implements IEmbeddedStoreService {
    private static final Logger logger = Logger.getLogger(EmbeddedStoreService.class);

    @Autowired
    private ApplicationContext context;

    private ConcurrentMap<String, EmbeddedStore> embeddedStoreMap = new ConcurrentHashMap<>();

    private EmbeddedStore schemaStore(String schema) {
        return embeddedStoreMap.computeIfAbsent(schema, k -> {
            EmbeddedStore newStore = new EmbeddedStore(k);
            context.getAutowireCapableBeanFactory().autowireBean(newStore);
            context.getAutowireCapableBeanFactory().initializeBean(newStore, "newStore-" + schema);
            return newStore;
        });
    }

    @Override
    @Timed(value = "llmka.embeddedstoreservice.store_time", description = "time to store")
    public void store(String schema, List<TextSegment> segments, List<Embedding> embeddings) {
        schemaStore(schema).store(segments, embeddings);
    }

    @Override
    public void removeIDes(String schema, Collection<String> idList) {
        schemaStore(schema).removeIDes(idList);
    }

    @Override
    public void removeOtherIDes(String schema, Collection<String> idList) {
        schemaStore(schema).removeOtherIDes(idList);
    }


    @Override
    @Timed(value = "llmka.embeddedstoreservice.retrieve_time", description = "time to retrieve similar items for single query")
    public Optional<List<Content>> retrieve(String schema, Embedding embeddedQuery, int maxResult, double minScoreLimit) {
        return schemaStore(schema).retrieve(embeddedQuery, maxResult, minScoreLimit);
    }


    @Override
    @Timed(value = "llmka.embeddedstoreservice.retrieve_list_time", description = "time to retrieve similar items for the first of (list)")
    public Optional<List<Content>> retrieve(String schema, List<Embedding> embeddings, int maxResult, double minScoreLimit) {
        for (Embedding embedding : embeddings) {
            Optional<List<Content>> contentList = retrieve(schema, embedding, maxResult, minScoreLimit);
            if (!contentList.isEmpty() && !contentList.get().isEmpty())
                return contentList;
        }
        return Optional.empty();
    }


    @Override
    @Timed(value = "llmka.embeddedstoreservice.check_and_store_time", description = "time spent on checkAndStore")
    public Optional<List<Content>> checkAndStore(String schema, List<TextSegment> segments, List<Embedding> embeddingList, double minScoreLimit) {
        return schemaStore(schema).checkAndStore(segments, embeddingList, minScoreLimit);
    }
}
