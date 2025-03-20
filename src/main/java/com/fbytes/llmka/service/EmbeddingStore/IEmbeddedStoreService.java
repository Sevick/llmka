package com.fbytes.llmka.service.EmbeddingStore;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IEmbeddedStoreService {
    void store(String schema, List<TextSegment> segments, List<Embedding> embeddingList);
    Optional<List<Content>> checkAndStore(String schema, List<TextSegment> segments, List<Embedding> embeddingList, double minScoreLimit);

    void removeIDes(String schema, Collection<String> idList);
    void removeOtherIDes(String schema, Collection<String> idList);

    Optional<List<Content>> retrieve(String schema, Embedding embeddedQuery, int maxResult, double minScoreLimit);
    Optional<List<Content>> retrieve(String schema, List<Embedding> embeddings, int maxResult, double minScoreLimit);
}
