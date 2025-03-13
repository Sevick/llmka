package com.fbytes.llmka.model.newssource;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.IConfigFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

@Service
public class NewsSourceFactory implements IConfigFactory<NewsSource> {
    private final static ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = Logger.getLogger(NewsSourceFactory.class);

    @PostConstruct
    private void init() {
        // search for NewsSource ancestors in the same package and register jackson subtypes
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(NewsSource.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(NewsSource.class.getPackageName().replaceAll("[.]", "/"));
        components.forEach(component -> {
            logger.debug("Register NewsSource subtype: {}", component.getBeanClassName());
            try {
                Class<?> newsSourceImplClass = Class.forName(component.getBeanClassName());
                String subType = getJsonClassAnnotationValue(newsSourceImplClass);
                mapper.registerSubtypes(new NamedType(newsSourceImplClass, subType));
            } catch (Exception e) {
                logger.logException(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    public NewsSource getParams(String json) {
        try {
            return mapper.readValue(json, NewsSource.class);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing json: {}. {}", json, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String getJsonClassAnnotationValue(Class<?> cl) {
        return ((JsonTypeName) Arrays.stream(cl.getAnnotations())
                .filter(a -> "com.fasterxml.jackson.annotation.JsonTypeName".equals(a.annotationType().getName()))
                .findAny().orElseThrow()).value();
    }
}
