package com.fbytes.llmka.service.Embedding.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
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

    @Value("${LLMka.embedding.model_path}")
    private String pathToModel;
    @Value("${LLMka.embedding.tokenizer_path}")
    private String pathToTokenizer;
    @Value("${LLMka.embedding.segment_length_limit:512}")
    private Integer segmentLengthLimit;
    @Value("${LLMka.embedding.segment_overlap:128}")
    private Integer segmentOverlap;

    private static final Logger logger = Logger.getLogger(EmbeddingService.class);

    private PoolingMode poolingMode = PoolingMode.MEAN;
    private EmbeddingModel embeddingModel;


    @PostConstruct
    private void init(){
        embeddingModel = new OnnxEmbeddingModel(pathToModel, pathToTokenizer, poolingMode);
        //embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    }



    @Override
    @Timed(value="llmka.embedding.time",description="time to embed NewsData",percentiles={0.5,0.9})
    public EmbeddedData embedNewsData(NewsData newsData) {
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("id", newsData.getId());
        metadataMap.put("link", newsData.getLink());
        Metadata metadata = Metadata.from(metadataMap);
        Document document = Document.from(newsData.getTitle() + "." + newsData.getDescription().orElse(""), metadata);
        DocumentSplitter splitter = DocumentSplitters.recursive(segmentLengthLimit, segmentOverlap);
        List<TextSegment> segments = splitter.split(document);
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        return new EmbeddedData(newsData, segments, embeddings);
    }


    @Override
    public Embedding embedStr(String inputStr) {
        return this.embeddingModel.embed(inputStr).content();
    }
}
