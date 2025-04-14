package com.fbytes.llmka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Optional;

@Data
@AllArgsConstructor
@Builder(toBuilder=true)
public class NewsData implements Cloneable{
    private String id;
    private String dataSourceID;
    private String dataSourceName;
    private String link;
    private String title;
    private Optional<String> description;
    private Optional<String> text;
    private Optional<Timestamp> pubDate;
    private Timestamp fetchDate;
    @Builder.Default
    private boolean rewritten = false;

    public String toText(){
        return title+description.orElse("");
    }

}
