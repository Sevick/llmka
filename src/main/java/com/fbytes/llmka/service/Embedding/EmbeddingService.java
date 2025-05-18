package com.fbytes.llmka.service.Embedding;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.tools.TextUtil;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService implements IEmbeddingService {
    private static final Logger logger = Logger.getLogger(EmbeddingService.class);

    @Value("${llmka.embedding.model_path}")
    private String pathToModel;
    @Value("${llmka.embedding.tokenizer_path}")
    private String pathToTokenizer;
    @Value("${llmka.embedding.segment_length_limit:512}")
    private Integer segmentLengthLimit;
    @Value("${llmka.embedding.segment_overlap:128}")
    private Integer segmentOverlap;

    private PoolingMode poolingMode = PoolingMode.CLS;
    private EmbeddingModel embeddingModel;


    @PostConstruct
    private void init() {
        embeddingModel = new OnnxEmbeddingModel(pathToModel, pathToTokenizer, poolingMode);
        logger.info("Initialized Model {} Tokenizer {}", pathToModel, pathToTokenizer);
    }


    @Override
    @Timed(value = "llmka.embedding.time", description = "time to embed NewsData", percentiles = {0.5, 0.9})
    public EmbeddedData embedNewsData(NewsData newsData) {
        logger.debug("newsData processing");
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("id", newsData.getExtID());
        metadataMap.put("link", newsData.getLink());
        Metadata metadata = Metadata.from(metadataMap);
        String normalizedTitle = TextUtil.normalize(newsData.getTitle());
        String normalizedDescr = newsData.getDescription().map(TextUtil::normalize).orElse("");
        TextUtil.trimToLength(normalizedDescr, segmentLengthLimit - normalizedTitle.length());
        Document document = Document.from(normalizedTitle + normalizedDescr, metadata);
        DocumentSplitter splitter = DocumentSplitters.recursive(segmentLengthLimit, segmentOverlap);
        List<TextSegment> segments = splitter.split(document);
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        logger.trace("newsData - produced {} TextSegments and {} embeddings", segments.size(), embeddings.size());
        return new EmbeddedData(newsData, segments, embeddings);
    }


    @Override
    public Embedding embedStr(String inputStr) {
        logger.debug("Processing string: {}", inputStr);
        return this.embeddingModel.embed(inputStr).content();
    }
}
