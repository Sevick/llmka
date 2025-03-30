package com.fbytes.llmka.service.EmbeddedStore.dao;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IEmbeddedStore {
    void store(List<TextSegment> segments, List<Embedding> embeddingList);
    Optional<List<Content>> checkAndStore(List<TextSegment> segments, List<Embedding> embeddingList, double minScoreLimit);

    void removeIDes(Collection<String> idList);
    void removeOtherIDes(Collection<String> idList);

    Optional<List<Content>> retrieve(Embedding embeddedQuery, int maxResult, double minScoreLimit);
    Optional<List<Content>> retrieve(List<Embedding> embeddings, int maxResult, double minScoreLimit);

    void save();
}
