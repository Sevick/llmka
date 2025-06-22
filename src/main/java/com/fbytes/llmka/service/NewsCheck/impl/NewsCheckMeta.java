package com.fbytes.llmka.service.NewsCheck.impl;

import com.fbytes.llmka.config.profiles.metrics.ParamTimedMetric;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.INewsIDStore;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Qualifier("newsCheckMeta")
public class NewsCheckMeta implements INewsCheck, INewsIDStore {
    private static final Logger logger = Logger.getLogger(NewsCheckMeta.class);

    @Value("${llmka.newscheck.metacheck.enabled:true}")
    private Boolean serviceEnabled;
    @Value("${llmka.newscheck.metacheck.metahash_size_limit:64}")
    private Integer metaHashSizeLimit;
    @Value("${llmka.newscheck.metacheck.metahash_size_core:32}")
    private Integer metaHashSizeCore;


    @Autowired
    private GenericApplicationContext context;
    @Autowired
    private ISchemaMetaServiceFactory schemaMetaServiceFactory;

    private final ConcurrentMap<String, NewsCheckMetaSchema> metaHash = new ConcurrentHashMap<>();   // <schemaName, NewsCheckMetaSchema>

    @Override
    @ParamTimedMetric(key = "schema")
    public Optional<RejectReason> checkNews(String schema, NewsData newsData) {
        if (!serviceEnabled)
            return Optional.empty();
        NewsCheckMetaSchema newsCheckMetaSchema = metaHash.computeIfAbsent(schema, str -> schemaMetaServiceFactory.createSchemaMetaService(schema));
        logger.debug("metaHash check");
        return newsCheckMetaSchema.checkNews(schema, newsData);
    }

    @Override
    public Set<String> fetchIDList(String schema) {
        return metaHash.get(schema).fetchIDList(schema);
    }
}
