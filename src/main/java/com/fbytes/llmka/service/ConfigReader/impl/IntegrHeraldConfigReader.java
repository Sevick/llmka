package com.fbytes.llmka.service.ConfigReader.impl;

import com.fbytes.llmka.model.heraldchannel.Herald;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IntegrHeraldConfigReader{

    @Value("${llmka.config.ignore_invalid_config:true}") private Boolean ignoreInvalidConfig;

    @Bean
    public ConfigReader<Herald> heraldConfigReader() {
        return new ConfigReader<Herald>(ignoreInvalidConfig);
    }
};