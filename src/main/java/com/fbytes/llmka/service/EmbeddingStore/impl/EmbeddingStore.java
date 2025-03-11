package com.fbytes.llmka.service.EmbeddingStore.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsCheckRejectReason;
import com.fbytes.llmka.service.EmbeddingStore.IEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Repository
public class EmbeddingStore implements IEmbeddingStore {
    @Value("${llmka.datastore.save_interval}")
    private Duration saveInterval;
    @Value("${llmka.datastore.store_path}")
    private String storeFilePath;

    private static final Logger logger = Logger.getLogger(EmbeddingStore.class);
    private final static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    private Instant lastStoreSave;

    @PostConstruct
    private void init() {
        lastStoreSave = Instant.now();
        try {
            restore(storeFilePath);
        } catch (Exception e) {
            logger.logException(e);
        }
    }


    @Override
    @Timed(value = "llmka.embeddingstore.store_time", description = "time to check news for duplicates")
    public void store(List<TextSegment> segments, List<Embedding> embeddings) {
        try {
            readWriteLock.writeLock().lock();
            embeddingStore.addAll(embeddings, segments);
            if (Duration.between(lastStoreSave, Instant.now()).compareTo(saveInterval) > 0){
                lastStoreSave = Instant.now();
                save(storeFilePath);
            }
        }
        finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void removeIDes(Collection<String> idList){
        try{
            readWriteLock.writeLock().lock();
            embeddingStore.removeAll(idList);
            save(storeFilePath);
            logger.debug("Removed {} entries from keystore: {}", idList.size(), idList);
        }
        finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void removeOtherIDes(Collection<String> idList){
        embeddingStore.removeAll(el -> !idList.contains(el));
        save(storeFilePath);
        logger.debug("Store cleaned up. Entries left: {}", idList.size());
    }



    @Override
    @Timed(value = "llmka.embeddingstore.retrieve_time", description = "time to check news for duplicates")
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
        for (int i = 0; i < embeddings.size(); i++) {
            Optional<List<Content>> contentList = retrieve(embeddings.get(i), maxResult, minScoreLimit);
            if (!contentList.isEmpty() && !contentList.get().isEmpty())
                return contentList;
        }
        return Optional.empty();
    }

    private void save(String storeFilePath) {
        logger.info("Saving store to: {}", storeFilePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storeFilePath))) {
            writer.write(embeddingStore.serializeToJson());
        } catch (IOException e) {
            logger.logException(e);
        }
    }

    private void restore(String storeFilePath) {
        embeddingStore = InMemoryEmbeddingStore.fromFile(storeFilePath);
        logger.info("Store restored from: {}", storeFilePath);
    }


    @Override
    public Optional<List<Content>> checkAndStore(List<TextSegment> segments, List<Embedding> embeddingList, double minScoreLimit){
        Optional<List<Content>> result = retrieve(embeddingList, 1, minScoreLimit);
        if (result.isEmpty()) {
            try {
                readWriteLock.writeLock().lock();
                Optional<List<Content>> recheckResult = retrieve(embeddingList, 1, minScoreLimit);
                if (!recheckResult.isEmpty())
                    return recheckResult;
                store(segments, embeddingList);
            }
            finally {
                readWriteLock.writeLock().unlock();
            }
            return Optional.empty();
        } else {
            return result;
        }
    }
}
