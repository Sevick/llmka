package com.fbytes.llmka.model.datasource;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fbytes.llmka.logger.Logger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

@Service
public class DataSourceFactory {
    private final static ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = Logger.getLogger(DataSourceFactory.class);

    @PostConstruct
    private void init() {
        // search for DataSource ancestors in the same package and register jackson subtypes
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(DataSource.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(DataSource.class.getPackageName().replaceAll("[.]", "/"));
        components.forEach(component -> {
            logger.debug("Register DataSource subtype: {}", component.getBeanClassName());
            try {
                Class<?> datasourceImplClass = Class.forName(component.getBeanClassName());
                String subType = getJsonClassAnnotationValue(datasourceImplClass);
                mapper.registerSubtypes(new NamedType(datasourceImplClass, subType));
            } catch (Exception e) {
                logger.logException(e);
                throw new RuntimeException(e);
            }
        });
    }

    public DataSource getDataSourceParams(String json) throws JsonProcessingException {
        return mapper.readValue(json, DataSource.class);
    }

    private String getJsonClassAnnotationValue(Class<?> cl) {
        return ((JsonTypeName) Arrays.stream(cl.getAnnotations())
                .filter(a -> "com.fasterxml.jackson.annotation.JsonTypeName".equals(a.annotationType().getName()))
                .findAny().orElseThrow()).value();
    }
}
