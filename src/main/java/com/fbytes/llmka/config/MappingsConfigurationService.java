package com.fbytes.llmka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbytes.llmka.model.config.MappingsConfiguration;
import com.fbytes.llmka.model.config.Mapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class MappingsConfigurationService {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${llmka.config.mappings_file}")
    private String mappingConfigFile;

    @Bean
    public MappingsConfiguration mappingConfigService() {
        try {
            String content = Files.readString(Path.of(mappingConfigFile));
            return new MappingsConfiguration(mapper.readValue(content, Mapping[].class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
