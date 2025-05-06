package com.fbytes.llmka.service.Maintenance;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.EmbeddedStore.EmbeddedStoreService;
import com.fbytes.llmka.service.NewsCheck.impl.NewsCheckMeta;
import com.fbytes.llmka.service.NewsCheck.impl.NewsCheckMetaSchema;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MaintenanceService implements IMaintenanceService {
    private static final Logger logger = Logger.getLogger(MaintenanceService.class);

    @Value("${llmka.newscheck.metacheck.schema_bean_prefix}")
    private String schemaBeanPrefix;
    @Value("${llmka.newscheck.metacheck.metahash_size_core:32}")
    private Integer metaHashSizeCore;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    private EmbeddedStoreService embeddedStoreService;
    @Autowired
    @Qualifier("newsCheckMeta")
    private NewsCheckMeta newsCheckMeta;


    @Override
    @Timed(value = "llmka.newsdatacheck.compressmeta.time", description = "time to cleanup the store")
    public void compressMeta(String schema, Integer size) {
        NewsCheckMetaSchema schemaMeta = (NewsCheckMetaSchema) applicationContext.getBean(schemaBeanPrefix + schema);
        schemaMeta.compressMetaHash(size);
    }

    @Override
    //@Timed(value = "llmka.newsdatacheck.cleanupstore.time", description = "time to cleanup the store")
    public void compressDB(String schema) {
        cleanupStore(schema);
    }

    public void compressDB() {
        embeddedStoreService.retieveSchemas().forEach(schema -> {
            cleanupStore(schema);
        });
    }

    // remove all IDes, that are not in metaHash

    public void cleanupStore(String schema) {
        logger.info("cleanupStore. Schema = {}", schema);
        Set<String> idsSet = newsCheckMeta.fetchIDList(schema);
        embeddedStoreService.removeOtherIDes(schema, idsSet);
    }
}
