package com.fbytes.llmka.service.EmbeddedStore.dao.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.EmbeddedStore.dao.IEmbeddedStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class EmbeddedStore implements IEmbeddedStore {
    private static final Logger logger = Logger.getLogger(EmbeddedStore.class);

    @Value("${llmka.datastore.save_interval}")
    private Duration saveInterval;
    @Value("${llmka.datastore.store_path}")
    private String storePath;
    @Value("${llmka.datastore.store_extension}")
    private String storeExtension;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    private final static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    private Instant lastStoreSave;

    private final String storeName;
    private final boolean loadDataOnInit;
    private String storeFilePath;

    private Gauge storeSizeGauge;


    public EmbeddedStore(String storeName, boolean loadDataOnInit) {
        this.storeName = storeName;
        this.loadDataOnInit = loadDataOnInit;
    }

    @PostConstruct
    private void init() {
        logger.debug("Initializing store: {}", storeName);
        if (meterRegistry != null) {
            storeSizeGauge = Gauge.builder("llmka.embeddedstore.store_size", () -> retrieveStoreSize())
                    .tag("name", storeName)
                    .description("Size of in-memory store")
                    .register(meterRegistry);
        }
        lastStoreSave = Instant.now();
        storeFilePath = storePath + storeName + storeExtension;
        if (loadDataOnInit) {
            try {
                restore(storeFilePath);
            } catch (Exception e) {
                logger.logException(e);
            }
        }
    }

    @PreDestroy
    private void finilize() {
        logger.debug("Finilizing store: {}", storeName);
        Metrics.globalRegistry.remove(storeSizeGauge);
    }


    @Override
    @Timed(value = "llmka.embeddedstore.store_time", description = "time to check news for duplicates")
    public void store(List<TextSegment> segments, List<Embedding> embeddings) {
        try {
            readWriteLock.writeLock().lock();
            embeddingStore.addAll(embeddings, segments);
            if (Duration.between(lastStoreSave, Instant.now()).compareTo(saveInterval) > 0) {
                lastStoreSave = Instant.now();
                save();
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }


    @Override
    public void removeIDes(Collection<String> idList) {
        try {
            readWriteLock.writeLock().lock();
            embeddingStore.removeAll(idList);
            save();
            logger.debug("Removed {} entries from keystore: [{}]", idList.size(), idList);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }


    @Override
    public void removeOtherIDes(Collection<String> idList) {
        embeddingStore.removeAll(el -> !idList.contains(((Metadata) el).getString("id")));
        save();
        logger.debug("Store [{}] cleaned up. Entries left: {}", storeName, idList.size());
    }


    @Override
    @Timed(value = "llmka.embeddedstore.retrieve_time", description = "time to check news for duplicates")
    public Optional<List<Content>> retrieve(Embedding embeddedQuery, int maxResult, double minScoreLimit) {
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddedQuery)
                .maxResults(maxResult)
                .minScore(minScoreLimit)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = this.embeddingStore.search(searchRequest);
        List<Content> result = searchResult.matches().stream()
                .map(embeddingMatch ->
                        Content.from(embeddingMatch.embedded(), Map.of(ContentMetadata.SCORE, embeddingMatch.score(),
                                ContentMetadata.EMBEDDING_ID, embeddingMatch.embeddingId()))
                ).collect(Collectors.toList());
        if (!result.isEmpty())
            return Optional.of(result);
        else
            return Optional.empty();
    }


    @Override
    public Optional<List<Content>> retrieve(List<Embedding> embeddings, int maxResult, double minScoreLimit) {
        return embeddings.stream()
                .map(emb -> retrieve(emb, maxResult, minScoreLimit))
                .filter(opt -> !opt.isEmpty())
                .findFirst().orElse(Optional.empty());
    }


    @Override
    public Optional<List<Content>> checkAndStore(List<TextSegment> segments, List<Embedding> embeddingList, double minScoreLimit) {
        Optional<List<Content>> result = retrieve(embeddingList, 1, minScoreLimit);
        if (result.isEmpty()) {
            try {
                readWriteLock.writeLock().lock();
                Optional<List<Content>> recheckResult = retrieve(embeddingList, 1, minScoreLimit);
                if (!recheckResult.isEmpty())
                    return recheckResult;
                store(segments, embeddingList);
            } finally {
                readWriteLock.writeLock().unlock();
            }
            return Optional.empty();
        } else {
            return result;
        }
    }

    private void save(String storeFilePath) {
        logger.info("Saving store to: {}", storeFilePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storeFilePath))) {
            writer.write(embeddingStore.serializeToJson());
        } catch (IOException e) {
            logger.logException(e);
        }
    }

    public void save() {
        save(storeFilePath);
    }

    @Override
    public void cleanStorage() {
        (new File(storeFilePath)).delete();
    }

    private void restore(String storeFilePath) {
        try {
            InMemoryEmbeddingStore newEmbeddingStore = InMemoryEmbeddingStore.fromFile(storeFilePath);
            embeddingStore = newEmbeddingStore;
        } catch (Exception e) {
            logger.warn("Unable to restore from file: {}", storeFilePath);
        }
        logger.info("Store restored from: {}", storeFilePath);
    }


    private Integer retrieveStoreSize() {
        try {
            Field storeEntriesField = embeddingStore.getClass().getDeclaredField("entries");
            storeEntriesField.setAccessible(true);
            List<?> fieldValue = (List<?>) storeEntriesField.get(embeddingStore);
            //storeEntriesField.setAccessible(false);
            if (fieldValue != null)
                return fieldValue.size();
            else
                return -1;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return -1;
        }
    }
}
