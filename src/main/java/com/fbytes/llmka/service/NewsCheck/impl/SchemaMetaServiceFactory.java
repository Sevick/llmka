package com.fbytes.llmka.service.NewsCheck.impl;

import com.fbytes.llmka.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class SchemaMetaServiceFactory implements ISchemaMetaServiceFactory {
    private static final Logger logger = Logger.getLogger(SchemaMetaServiceFactory.class);

    @Value("${llmka.newscheck.metacheck.schema_bean_prefix}")
    private String schemaBeanPrefix;

    private final ApplicationContext applicationContext;

    public SchemaMetaServiceFactory(@Autowired ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public NewsCheckMetaSchema createSchemaMetaService(String schema) {
        logger.debug("[{}] Creating NewsCheckMetaSchema", schema);
        String beanName = schemaBeanPrefix + schema;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(NewsCheckMetaSchema.class);
        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        builder.addConstructorArgValue(schema); // Schema name
        builder.setScope("singleton"); // Set scope to singleton
        beanFactory.registerBeanDefinition(beanName, builder.getBeanDefinition());
        beanFactory.getBean(beanName, NewsCheckMetaSchema.class);
        return beanFactory.getBean(beanName, NewsCheckMetaSchema.class);
    }
}
