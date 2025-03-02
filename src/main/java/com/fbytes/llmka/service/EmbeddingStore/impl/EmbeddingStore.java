package com.fbytes.llmka.service.EmbeddingStore.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.EmbeddingStore.IEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class EmbeddingStore implements IEmbeddingStore {
    @Value("${LLMka.datastore.save_interval}")
    private Duration saveInterval;
    @Value("${LLMka.datastore.store_path}")
    private String storeFilePath;

    private static final Logger logger = Logger.getLogger(EmbeddingStore.class);

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
    public void store(List<TextSegment> segments, List<Embedding> embeddings) {
        // Issues with thread safety
        synchronized (this) {
            embeddingStore.addAll(embeddings, segments);
            if (Duration.between(lastStoreSave, Instant.now()).compareTo(saveInterval) > 0){
                save(storeFilePath);
                lastStoreSave = Instant.now();
            }
        }
    }

    public void removeIDes(List<String> idList){
        embeddingStore.removeAll(idList);
        logger.debug("Removed {} entries from keystore: {}", idList.size(), idList);
    }


    @Override
    public Optional<List<Content>> retrieve(Embedding embeddedQuery, int maxResult, double MinScoreLimit) {
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddedQuery)
                .maxResults(maxResult)
                .minScore(MinScoreLimit)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = this.embeddingStore.search(searchRequest);
        List<Content> result = searchResult.matches().stream()
                .map(embeddingMatch ->
                        Content.from(embeddingMatch.embedded(), Map.of(ContentMetadata.SCORE, embeddingMatch.score(),
                                ContentMetadata.EMBEDDING_ID, embeddingMatch.embeddingId()))
                ).collect(Collectors.toList());
        if (result != null && !result.isEmpty())
            return Optional.of(result);
        else
            return Optional.empty();
    }


    @Override
    public Optional<List<Content>> retrieve(List<Embedding> embeddings, int maxResult, double minScoreLimit) {
        for (int i = 0; i < embeddings.size(); i++) {
            Optional<List<Content>> contentList = retrieve(embeddings.get(i), maxResult, minScoreLimit);
            if (!contentList.isEmpty())
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
}
