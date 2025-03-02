package com.fbytes.llmka.service.EmbeddingStore;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;

import java.util.List;
import java.util.Optional;

public interface IEmbeddingStore {
    void store(List<TextSegment> segments, List<Embedding> embeddingList);

    void removeIDes(List<String> idList);

    Optional<List<Content>> retrieve(Embedding embeddedQuery, int maxResult, double MinScoreLimit);

    Optional<List<Content>> retrieve(List<Embedding> embeddings, int maxResult, double MinScoreLimit);
}
