package com.fbytes.llmka.service.InMemoryFastStore;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.store.embedding.*;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dev.langchain4j.spi.ServiceHelper.loadFactories;

public class InMemoryFastStore<Embedded> implements EmbeddingStore<Embedded> {
    final private ConcurrentHashMap<String, Entry<Embedded>> entries = new ConcurrentHashMap<>(); // <id, Entry>

    @Override
    public String add(Embedding embedding) {
        String id = UUID.randomUUID().toString();
        this.add(id, embedding);
        return id;
    }

    @Override
    public void add(String id, Embedding embedding) {
        this.add(id, embedding, (Embedded) null);
    }

    @Override
    public String add(Embedding embedding, Embedded embedded) {
        String id = UUID.randomUUID().toString();
        this.add(id, embedding, embedded);
        return id;
    }

    private void add(String id, Embedding embedding, Embedded embedded) {
        this.entries.put(id, new Entry(id, embedding, embedded));
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        List<Entry<Embedded>> newEntries = (List) embeddings.stream().map((embedding) -> new Entry(Utils.randomUUID(), embedding)).collect(Collectors.toList());
        return this.add(newEntries);
    }

    @Override
    public void addAll(List<String> ids, List<Embedding> embeddings, List<Embedded> embedded) {
        if (ids.size() == embeddings.size() && embeddings.size() == embedded.size()) {
            List<Entry<Embedded>> newEntries = new ArrayList(ids.size());
            for (int i = 0; i < ids.size(); ++i) {
                newEntries.add(new Entry((String) ids.get(i), (Embedding) embeddings.get(i), embedded.get(i)));
            }
            this.add(newEntries);
        } else {
            throw new IllegalArgumentException("The list of ids and embeddings and embedded must have the same size");
        }
    }


    private List<String> add(List<Entry<Embedded>> newEntries) {
        List<String> addedIDes = new ArrayList<>(newEntries.size());
        newEntries.forEach(entry -> {
            Entry res = entries.putIfAbsent(entry.getId(), entry);
            if (res != null) {
                addedIDes.add(res.getId());
            }
        });
        return addedIDes;
    }

    @Override
    public void removeAll(Collection<String> ids) {
        ValidationUtils.ensureNotEmpty(ids, "ids");
        ids.forEach(id -> entries.remove(id));
    }


    @Override
    public EmbeddingSearchResult<Embedded> search(EmbeddingSearchRequest request) {
        if (request.filter() != null) {
            throw new UnsupportedOperationException("EmbeddingSearchRequest.Filter is not supported yet.");
        }
        double queryNorm = FastCosineSimilarity.norm(request.queryEmbedding());
        List<EmbeddingMatch<Embedded>> matches = entries.entrySet().stream()
                .map(entry -> {
                    double cosineSimilarity = FastCosineSimilarity.between(entry.getValue().getEmbedding(), request.queryEmbedding(), entry.getValue().getNorm(), queryNorm);
                    double score = RelevanceScore.fromCosineSimilarity(cosineSimilarity);
                    return new EmbeddingMatch<Embedded>(score, entry.getKey(), entry.getValue().getEmbedding(), entry.getValue().getEmbedded());
                })
                .filter(embeddingMatch -> embeddingMatch.score() >= request.minScore())
                .limit(request.maxResults())
                .toList();
        return new EmbeddingSearchResult<>(matches);
    }

    public String serializeToJson() {
        return loadCodec().toJson(this);
    }

    public void serializeToFile(Path filePath) {
        try {
            String json = this.serializeToJson();
            Files.write(filePath, json.getBytes(), new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void serializeToFile(String filePath) {
        this.serializeToFile(Paths.get(filePath));
    }

    public static InMemoryFastStore<TextSegment> fromJson(String json) {
        return loadCodec().fromJson(json);
    }

    public static InMemoryFastStore<TextSegment> fromFile(Path filePath) {
        try {
            String json = new String(Files.readAllBytes(filePath));
            return fromJson(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InMemoryFastStore<TextSegment> fromFile(String filePath) {
        return fromFile(Paths.get(filePath));
    }


    private static InMemoryFastStoreJsonCodec loadCodec() {
        for (InMemoryFastStoreJsonCodecFactory factory : loadFactories(InMemoryFastStoreJsonCodecFactory.class)) {
            return factory.create();
        }
        return new JacksonInMemoryFastStoreJsonCodec();
    }

    public Set<String> fetchAllIDes() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    @Getter
    static class Entry<Embedded> {
        private final String id;
        private final Embedding embedding;
        private final Embedded embedded;
        private final Double norm;

        Entry(String id, Embedding embedding) {
            this(id, embedding, (Embedded) null);
        }

        Entry(String id, Embedding embedding, Embedded embedded) {
            this.id = ValidationUtils.ensureNotBlank(id, "id");
            this.embedding = (Embedding) ValidationUtils.ensureNotNull(embedding, "embedding");
            this.embedded = embedded;
            this.norm = FastCosineSimilarity.norm(embedding);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                Entry<?> that = (Entry) o;
                return Objects.equals(this.id, that.id);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(this.id);
        }
    }
}
