package com.fbytes.llmka.model.config;

import com.fbytes.llmka.model.config.newsgroup.NewsGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NewsGroupsConfiguration {

    private Map<String, NewsGroup> newsGroupMap;
}
