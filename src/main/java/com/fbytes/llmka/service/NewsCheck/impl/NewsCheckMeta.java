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
    @Value("${llmka.newscheck.metacheck.schema_bean_prefix}")
    private String schemaBeanPrefix;

    @Autowired
    private GenericApplicationContext context;

    private final ConcurrentMap<String, NewsCheckMetaSchema> metaHash = new ConcurrentHashMap<>();   // <MD5, <seq#, id>>

    @Override
    @ParamTimedMetric(key = "schema")
    public Optional<RejectReason> checkNews(String schema, NewsData newsData) {
        if (!serviceEnabled)
            return Optional.empty();
        NewsCheckMetaSchema newsCheckMetaSchema = metaHash.computeIfAbsent(schema, str -> createSchemaMetaService(schema));
        logger.debug("metaHash check");
        return newsCheckMetaSchema.checkNews(schema, newsData);
    }

    public NewsCheckMetaSchema createSchemaMetaService(String schema) {
        logger.debug("[{}] Creating NewsCheckMetaSchema", schema);
        String beanName = schemaBeanPrefix + schema;
        context.registerBean(beanName, NewsCheckMetaSchema.class, () -> {
            return new NewsCheckMetaSchema(schema);
        });
        NewsCheckMetaSchema bean = (NewsCheckMetaSchema) context.getBean(beanName);
        //context.getAutowireCapableBeanFactory().autowireBean(bean);
        return bean;
    }

    @Override
    public Set<String> fetchIDList(String schema) {
        return metaHash.get(schema).fetchIDList(schema);
    }
}
