package com.fbytes.llmka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@AllArgsConstructor
@Builder
public class NewsData {
    private String id;
    private String dataSourceID;
    private String dataSourceName;
    private String link;
    private String title;
    private Optional<String> description;
    private Optional<String> text;
}
