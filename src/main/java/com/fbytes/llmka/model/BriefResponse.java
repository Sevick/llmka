package com.fbytes.llmka.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BriefResponse {
    @JsonProperty("title")
    @JsonPropertyDescription("The title of the news article")
    private String title;
    @JsonProperty("content")
    @JsonPropertyDescription("The content of the news article")
    private String content;

    public static JsonSchema toLLMResonseSchema() {
        return JsonSchema.builder()
                .name("BriefResponse")
                .rootElement(JsonObjectSchema.builder()
                        .addStringProperty("title", "Title of the news article")
                        .addStringProperty("content", "Content of the news article")
                        .required("title", "content")
                        .build())
                .build();
    }
}
