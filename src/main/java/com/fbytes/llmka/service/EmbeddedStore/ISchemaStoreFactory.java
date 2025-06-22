package com.fbytes.llmka.service.EmbeddedStore;

import com.fbytes.llmka.service.EmbeddedStore.dao.IEmbeddedStore;
import org.springframework.context.support.GenericApplicationContext;

public interface ISchemaStoreFactory {
    IEmbeddedStore createEmbeddedStoreService(String schema);
}
