package com.fbytes.llmka.config;

import com.fbytes.llmka.model.config.HeraldsConfiguration;
import com.fbytes.llmka.model.config.IConfigFactory;
import com.fbytes.llmka.model.config.heraldchannel.HeraldConfig;
import com.fbytes.llmka.model.config.newsgroup.NewsGroup;
import com.fbytes.llmka.model.config.newssource.NewsSource;
import com.fbytes.llmka.service.ConfigReader.ConfigReader;
import com.fbytes.llmka.service.ConfigReader.IConfigReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;

@Configuration
public class ConfigReadersConfiguration {
    private final Boolean ignoreInvalidConfig;
    private final String heraldConfigFilePath;

    public ConfigReadersConfiguration(@Value("${llmka.herald.config_file}") String heraldConfigFilePath,
                                      @Value("${llmka.config.ignore_invalid_config:true}") Boolean ignoreInvalidConfig) {
        this.heraldConfigFilePath = heraldConfigFilePath;
        this.ignoreInvalidConfig = ignoreInvalidConfig;
    }

    @Bean
    public ConfigReader<HeraldConfig> heraldConfigReader() {
        return new ConfigReader<HeraldConfig>(ignoreInvalidConfig);
    }

    @Bean
    public ConfigReader<NewsSource> newsSourceConfigReader() {
        return new ConfigReader<NewsSource>(ignoreInvalidConfig);
    }

    @Bean
    public ConfigReader<NewsGroup> newsGroupsConfigReader() {
        return new ConfigReader<NewsGroup>(ignoreInvalidConfig);
    }


    @Bean
    public HeraldsConfiguration heraldConfigService(
            @Autowired IConfigReader<HeraldConfig> heraldConfigReader,
            @Autowired IConfigFactory<HeraldConfig> heraldFactory) {
        List<HeraldConfig> heraldList = heraldConfigReader.retrieveFromFile(heraldFactory, new File(heraldConfigFilePath));
        return new HeraldsConfiguration(heraldList.toArray(size -> new HeraldConfig[size]));
    }
}
