package com.fbytes.llmka.service.EmbeddedStore;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.EmbeddedStore.dao.EmbeddedStore;
import com.fbytes.llmka.service.EmbeddedStore.dao.IEmbeddedStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class EmbeddedStoreService implements IEmbeddedStoreService {
    private static final Logger logger = Logger.getLogger(EmbeddedStoreService.class);

    @Autowired
    private GenericApplicationContext applicationContext;

    private final ConcurrentMap<String, IEmbeddedStore> embeddedStoreMap = new ConcurrentHashMap<>(); // <schema, store>


    @PreDestroy
    private void onShutdown() {
        embeddedStoreMap.forEach((key, value) -> value.save());
    }


    private IEmbeddedStore createSchemaStore(String schema) {
        String beanName = "newsStore-" + schema;
        return embeddedStoreMap.computeIfAbsent(schema, name -> {
            applicationContext.registerBean(beanName, EmbeddedStore.class, () ->
                    new EmbeddedStore(name, true)
            );
            return (IEmbeddedStore) applicationContext.getBean(beanName);
        });
    }

    @Override
    @Timed(value = "llmka.embeddedstoreservice.store_time", description = "time to store", percentiles = {0.5, 0.9})
    public void store(String schema, List<TextSegment> segments, List<Embedding> embeddings) {
        createSchemaStore(schema).store(segments, embeddings);
    }

    @Override
    public void removeIDes(String schema, Collection<String> idList) {
        createSchemaStore(schema).removeIDes(idList);
    }

    @Override
    public void removeOtherIDes(String schema, Collection<String> idList) {
        createSchemaStore(schema).removeOtherIDes(idList);
    }


    @Override
    @Timed(value = "llmka.embeddedstoreservice.retrieve_time", description = "time to retrieve similar items for single query", percentiles = {0.5, 0.9})
    public Optional<List<Content>> retrieve(String schema, Embedding embeddedQuery, int maxResult, double minScoreLimit) {
        return createSchemaStore(schema).retrieve(embeddedQuery, maxResult, minScoreLimit);
    }


    @Override
    @Timed(value = "llmka.embeddedstoreservice.retrieve_list_time", description = "time to retrieve similar items for the first of (list)", percentiles = {0.5, 0.9})
    public Optional<List<Content>> retrieve(String schema, List<Embedding> embeddings, int maxResult, double minScoreLimit) {
        for (Embedding embedding : embeddings) {
            Optional<List<Content>> contentList = retrieve(schema, embedding, maxResult, minScoreLimit);
            if (!contentList.isEmpty() && !contentList.get().isEmpty())
                return contentList;
        }
        return Optional.empty();
    }

    @Override
    public Set<String> retieveSchemas() {
        return embeddedStoreMap.keySet();
    }


    @Override
    @Timed(value = "llmka.embeddedstoreservice.check_and_store_time", description = "time spent on checkAndStore", percentiles = {0.5, 0.9})
    public Optional<List<Content>> checkAndStore(String schema, List<TextSegment> segments, List<Embedding> embeddingList, double minScoreLimit) {
        return createSchemaStore(schema).checkAndStore(segments, embeddingList, minScoreLimit);
    }
}
