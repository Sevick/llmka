package com.fbytes.llmka.service.InMemoryFastStore;

import dev.langchain4j.data.segment.TextSegment;

public interface InMemoryFastStoreJsonCodec {
    InMemoryFastStore<TextSegment> fromJson(String var1);

    String toJson(InMemoryFastStore<?> var1);
}