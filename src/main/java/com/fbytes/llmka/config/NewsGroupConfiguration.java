package com.fbytes.llmka.config;

import com.fbytes.llmka.model.config.HeraldsConfiguration;
import com.fbytes.llmka.model.config.IConfigFactory;
import com.fbytes.llmka.model.config.NewsGroupsConfiguration;
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
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class NewsGroupConfiguration {

    private final String newsGroupConfigFilePath;

    public NewsGroupConfiguration(@Value("${llmka.config.newsgroups_file}") String newsGroupConfigFilePath) {
        this.newsGroupConfigFilePath = newsGroupConfigFilePath;
    }

    @Bean
    public NewsGroupsConfiguration newsGroupsConfiguration(
            @Autowired IConfigReader<NewsGroup> newsGroupConfigReader,
            @Autowired IConfigFactory<NewsGroup> newsGroupFactory) {
        List<NewsGroup> newsGroupList = newsGroupConfigReader.retrieveFromFile(newsGroupFactory, new File(newsGroupConfigFilePath));
        Map<String, NewsGroup> newsGroupMap = newsGroupList.stream().collect(
                Collectors.toMap(NewsGroup::getName, newsGroup -> newsGroup, (existing, replacement) -> existing
                ));
        return new NewsGroupsConfiguration(newsGroupMap);
    }
}
