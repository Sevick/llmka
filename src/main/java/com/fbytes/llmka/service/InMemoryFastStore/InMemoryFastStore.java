package com.fbytes.llmka.service.InMemoryFastStore;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryFastStore<Embedded> implements EmbeddingStore<Embedded> {
    final ConcurrentHashMap<String, Entry<Embedded>> entries = new ConcurrentHashMap<>();
    final ConcurrentHashMap<String, Long> preCalc = new ConcurrentHashMap<>(); // <id,vectorLength>

    @Override
    public String add(Embedding embedding) {
        return "";
    }

    @Override
    public void add(String s, Embedding embedding) {

    }

    @Override
    public String add(Embedding embedding, Embedded embedded) {
        return "";
    }

    @Override
    public List<String> addAll(List<Embedding> list) {
        return List.of();
    }


    private static class Entry<Embedded> {
        String id;
        Embedding embedding;
        Embedded embedded;
        double norm;

        Entry(String id, Embedding embedding) {
            this(id, embedding, (Embedded) null);
        }

        Entry(String id, Embedding embedding, Embedded embedded) {
            this.id = ValidationUtils.ensureNotBlank(id, "id");
            this.embedding = (Embedding) ValidationUtils.ensureNotNull(embedding, "embedding");
            this.embedded = embedded;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                InMemoryFastStore.Entry<?> that = (InMemoryFastStore.Entry) o;
                return Objects.equals(this.id, that.id) && Objects.equals(this.embedding, that.embedding) && Objects.equals(this.embedded, that.embedded);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.id, this.embedding, this.embedded});
        }
    }
}

