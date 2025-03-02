package com.fbytes.llmka.service.NewsDataCheck.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsCheckRejectReason;
import com.fbytes.llmka.service.EmbeddingStore.IEmbeddingStore;
import com.fbytes.llmka.service.NewsDataCheck.INewsDataCheck;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
public class NewsDataCheck implements INewsDataCheck {

    @Value("${LLMka.datacheck.metahash_size_limit:64}")
    private Integer metaHashSizeLimit;
    @Value("${LLMka.datacheck.metahash_size_core:32}")
    private Integer metaHashSizeCore;
    @Value("#{T(Float).parseFloat('${LLMka.datacheck.score_limit}')}")
    private Float scoreLimit;

    @Autowired
    private IEmbeddingStore embeddingStore;

    private AtomicInteger metaHashSeq = new AtomicInteger(0);
    private AtomicInteger metaHashSize = new AtomicInteger(0);
    private ReadWriteLock metaHashCompressLock = new ReentrantReadWriteLock();
    private Map<BigInteger, Pair<Integer, String>> metaHash = new ConcurrentHashMap<>();   // <MD5, <seq#,id>>

    private static final Logger logger = Logger.getLogger(NewsDataCheck.class);


    @Override
    public Pair<Boolean, Optional<NewsCheckRejectReason>> checkNewsData(EmbeddedData embeddedData) {
        if (!checkMeta(embeddedData))
            return Pair.of(false, Optional.of(new NewsCheckRejectReason(NewsCheckRejectReason.REASON.META_DUPLICATION, "")));
        synchronized (this) {
            Optional<List<Content>> result = embeddingStore.retrieve(embeddedData.getEmbeddings(), 1, scoreLimit);
            if (result.isEmpty()) {
                embeddingStore.store(embeddedData.getSegments(), embeddedData.getEmbeddings());
                return Pair.of(true, Optional.empty());
            } else {
                return Pair.of(false, Optional.of(new NewsCheckRejectReason(NewsCheckRejectReason.REASON.CLOSE_MATCH, result.get().get(0).textSegment().text())));
            }
        }
    }


    // check hash of meta for each segment
    private boolean checkMeta(EmbeddedData embeddedData) {
        TextSegment firstSegment = embeddedData.getSegments().get(0);
        String id = firstSegment.metadata().getString("id");
        String metaStr = firstSegment.metadata().getString("link");
        metaStr = metaStr == null ? firstSegment.metadata().getString("title") : metaStr;
        Pair<Integer, String> result;
        try {
            metaHashCompressLock.readLock().lock();
            result = metaHash.putIfAbsent(calculateMD5Hash(metaStr), Pair.of(metaHashSeq.getAndIncrement(), id));
        } finally {
            metaHashCompressLock.readLock().unlock();
        }

        if (result != null) {
            return false;
        } else {
            if (metaHashSize.incrementAndGet() > metaHashSizeLimit) {
                try {
                    metaHashCompressLock.writeLock().lock();
                    Pair<Map<BigInteger, Pair<Integer, String>>, List<String>> compressionResult = compressMetaHash(metaHashSizeCore);
                    metaHash = compressionResult.getLeft();
                    embeddingStore.removeIDes(compressionResult.getRight());
                    metaHashSize.set(metaHashSizeCore);
                } finally {
                    metaHashCompressLock.writeLock().unlock();
                }
            }
            return true;
        }
    }


    // returns <newHashMap, List<removedIDes>
    private Pair<Map<BigInteger, Pair<Integer, String>>, List<String>> compressMetaHash(int reduceToSize) {
        logger.info("Compressing metaHash. Current size: {}", metaHash.size());
        Map<BigInteger, Pair<Integer, String>> newMetaMap = new ConcurrentHashMap();
        Map.Entry<BigInteger, Pair<Integer, String>>[] entriesArr = metaHash.entrySet().toArray(new Map.Entry[0]);
        Arrays.sort(entriesArr, Map.Entry.<BigInteger, Pair<Integer, String>>comparingByValue().reversed());

        ConcurrentHashMap<BigInteger, Pair<Integer, String>> newMetaHash = Arrays.stream(entriesArr, 0, reduceToSize)
                .collect(Collectors.toMap(entry -> entry.getKey(),
                        entry -> entry.getValue(),
                        (existing, replacement) -> existing,
                        ConcurrentHashMap::new));
        List<String> removedIdList = Arrays.stream(entriesArr, reduceToSize + 1, entriesArr.length - 1)
                .map(entry -> entry.getValue().getRight())
                .toList();

        return Pair.of(newMetaHash, removedIdList);
    }


    private BigInteger calculateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
