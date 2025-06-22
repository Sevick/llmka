package com.fbytes.llmka.service.EmbeddedStore;

import com.fbytes.llmka.service.EmbeddedStore.dao.EmbeddedStore;
import com.fbytes.llmka.service.EmbeddedStore.dao.IEmbeddedStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class SchemaStoreFactory implements ISchemaStoreFactory {

    private final GenericApplicationContext applicationContext;

    public SchemaStoreFactory(@Autowired GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public IEmbeddedStore createEmbeddedStoreService(String schema) {
        String beanName = "newsStore-" + schema;
        applicationContext.registerBean(beanName, EmbeddedStore.class, () ->
                new EmbeddedStore(schema, true)
        );
        return (IEmbeddedStore) applicationContext.getBean(beanName);
    }
}
