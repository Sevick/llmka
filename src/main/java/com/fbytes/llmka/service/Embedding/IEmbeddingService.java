package com.fbytes.llmka.service.Embedding;

import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface IEmbeddingService {
    EmbeddedData embedNewsData(NewsData newsData);

    Embedding embedStr(String inputStr);
}
