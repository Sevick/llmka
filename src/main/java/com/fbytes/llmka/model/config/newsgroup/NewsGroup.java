package com.fbytes.llmka.model.config.newsgroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsGroup {
    private String name;
    private String language;
    private String purpose;
    private Integer messageSizeLimit;
}
