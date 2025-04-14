package com.fbytes.llmka.service.Maintenance.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.EmbeddedStore.impl.EmbeddedStoreService;
import com.fbytes.llmka.service.Maintenance.IMaintenanceService;
import com.fbytes.llmka.service.NewsCheck.impl.NewsCheckMeta;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MaintenanceService implements IMaintenanceService {
    private static final Logger logger = Logger.getLogger(MaintenanceService.class);

    @Autowired
    private EmbeddedStoreService embeddedStoreService;
    @Autowired
    @Qualifier("newCheckMeta")
    private NewsCheckMeta newsCheckMeta;


    @Override
    @Timed(value = "llmka.newsdatacheck.cleanupstore.time", description = "time to cleanup the store", percentiles = {0.5, 0.9})
    public void compressDB(String schema) {
        cleanupStore(schema);
    }

    // remove all IDes, that are not in metaHash

    public void cleanupStore(String schema) {
        //logger.info("cleanupStore. Current hash size: {}", metaHash.size());
        Set<String> idsSet = newsCheckMeta.fetchIDList();
        embeddedStoreService.removeOtherIDes(schema, idsSet);
    }
}
