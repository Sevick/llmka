package com.fbytes.llmka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbytes.llmka.integration.service.HeraldConfigService;
import com.fbytes.llmka.model.IConfigFactory;
import com.fbytes.llmka.model.heraldchannel.Herald;
import com.fbytes.llmka.model.heraldchannel.HeraldFactory;
import com.fbytes.llmka.service.ConfigReader.IConfigReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;


@Configuration
public class IntegrHeraldConfigService {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String heraldConfigFilePath;

    public IntegrHeraldConfigService(@Value("${llmka.herald.config_file}") String heraldConfigFilePath) {
        this.heraldConfigFilePath = heraldConfigFilePath;
    }

    @Bean
    public HeraldConfigService heraldConfigService(
                                                   @Autowired IConfigReader<Herald> heraldConfigReader,
                                                   @Autowired IConfigFactory<Herald> heraldFactory) {
        List<Herald> heraldList = heraldConfigReader.retrieveFromFile(heraldFactory, new File(heraldConfigFilePath));
        return new HeraldConfigService(heraldList.toArray(size -> new Herald[size]));
    }
}