package com.fbytes.llmka.model.config.heraldchannel;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.config.IConfigFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

@Service
public class HeraldConfigFactory implements IConfigFactory<HeraldConfig> {
    private static final Logger logger = Logger.getLogger(HeraldConfigFactory.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    private void init() {
        // search for HeraldChannel ancestors in the same package and register jackson subtypes
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(HeraldConfig.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(HeraldConfig.class.getPackageName().replaceAll("[.]", "/"));
        components.forEach(component -> {
            logger.debug("Register HeraldChannel subtype: {}", component.getBeanClassName());
            try {
                Class<?> heraldChannelImplClass = Class.forName(component.getBeanClassName());
                String subType = getJsonClassAnnotationValue(heraldChannelImplClass);
                mapper.registerSubtypes(new NamedType(heraldChannelImplClass, subType));
            } catch (Exception e) {
                logger.logException(e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public HeraldConfig getParams(String json) throws JsonProcessingException {
        return mapper.readValue(json, HeraldConfig.class);
    }

    private String getJsonClassAnnotationValue(Class<?> cl) {
        return ((JsonTypeName) Arrays.stream(cl.getAnnotations())
                .filter(a -> "com.fasterxml.jackson.annotation.JsonTypeName".equals(a.annotationType().getName()))
                .findAny().orElseThrow()).value();
    }
}
