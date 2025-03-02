package com.fbytes.llmka.model.datasource;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("RSS")
public class RssDataSource extends DataSource {
    private String url;

    public RssDataSource() {
        super();
    }

    public RssDataSource(String id, String name, String url) {
        super(id, "RSS", name);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
