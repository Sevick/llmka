package com.fbytes.llmka.service.EmbeddedStore.dao;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.InMemoryFastStore.InMemoryFastStore;
import com.fbytes.llmka.tools.FileUtil;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
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
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private InMemoryFastStore<TextSegment> embeddingStore = new InMemoryFastStore<>();
    private Instant lastStoreSave;

    private final String storeName;
    private final boolean loadDataOnInit;
    private String storeFilePath;

    // keep ref to prevent GC
    private Gauge storeSizeGauge;


    public EmbeddedStore(String storeName, boolean loadDataOnInit) {
        this.storeName = storeName;
        this.loadDataOnInit = loadDataOnInit;
    }

    @PostConstruct
    public void init() {
        logger.debug("[{}] Initializing store", storeName);
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
    public void onShutdown() {
        logger.debug("[{}] Finalizing store", storeName);
        save();
        if (storeSizeGauge != null)
            Metrics.globalRegistry.remove(storeSizeGauge);
    }


    @Override
    @Timed(value = "llmka.embeddedstore.store_time", description = "time to check news for duplicates")
    public void store(List<TextSegment> segments, List<Embedding> embeddings) {
        List<String> ids = segments.stream()
                .map(textSegment -> textSegment.metadata().getString("id"))
                .toList();

        try {
            readWriteLock.writeLock().lock();
            embeddingStore.addAll(ids, embeddings, segments);
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
            logger.debug("[{}] Removed {} entries from keystore: [{}]", storeName, idList.size(), idList);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }


    @Override
    public void removeOtherIDes(Collection<String> idList) {
        logger.debug("[{}] Cleaning up store. Entries to keep: {}", storeName, idList.size());
        Set<String> idsToRemove = new HashSet<>(embeddingStore.fetchAllIDes());
        if (idsToRemove.isEmpty()) {
            logger.debug("[{}] There is nothing to cleanup. Entries left: {}", storeName, idList.size());
            return;
        }
        idsToRemove.removeAll(idList);
        embeddingStore.removeAll(idsToRemove);
        save();
        logger.info("[{}] Store cleaned up. Entries left: {}", storeName, idList.size());
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


    @Override
    public boolean removeStorage() {
        boolean result = (new File(storeFilePath)).delete();
        if (!result) {
            logger.warn("Unable to remove storage file: {}", storeFilePath);
        }
        return result;
    }


    public void save() {
        save(storeFilePath);
    }


    private void save(String storeFilePath) {
        logger.info("[{}] Saving store to: {}", storeName, storeFilePath);
        FileUtil.moveToBackup(storeFilePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storeFilePath))) {
            writer.write(embeddingStore.serializeToJson());
        } catch (IOException e) {
            logger.logException(e);
        }
    }


    private void restore(String storeFilePath) {
        try {
            InMemoryFastStore<TextSegment> newEmbeddingStore = InMemoryFastStore.fromFile(storeFilePath);
            embeddingStore = newEmbeddingStore;
        } catch (Exception e) {
            logger.logException(MessageFormat.format("[{0}] Unable to restore from file: {1}", storeName, storeFilePath), e);
        }
        logger.info("[{}] Store restored {} item from: {}", storeName, embeddingStore.fetchAllIDes().size(), storeFilePath);
    }


    private Integer retrieveStoreSize() {
        try {
            Field storeEntriesField = embeddingStore.getClass().getDeclaredField("entries");
            storeEntriesField.setAccessible(true);
            ConcurrentHashMap<?, ?> fieldValue = (ConcurrentHashMap<?, ?>) storeEntriesField.get(embeddingStore);
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
