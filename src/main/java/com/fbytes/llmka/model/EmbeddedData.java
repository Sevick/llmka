package com.fbytes.llmka.model;

import dev.langchain4j.data.segment.TextSegment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class EmbeddedData {
    private NewsData newsData;
    private List<TextSegment> segments;
    private List<dev.langchain4j.data.embedding.Embedding> embeddings;
}
