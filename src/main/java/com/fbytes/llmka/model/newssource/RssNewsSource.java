package com.fbytes.llmka.model.newssource;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("RSS")
public class RssNewsSource extends NewsSource {
    private String url;

    public RssNewsSource() {
        super();
    }

    public RssNewsSource(String id, String name, String url, String groupName) {
        super(id, "RSS", name, groupName);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
