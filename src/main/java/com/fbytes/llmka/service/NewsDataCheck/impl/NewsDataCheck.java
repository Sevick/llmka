package com.fbytes.llmka.service.NewsDataCheck.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsCheckRejectReason;
import com.fbytes.llmka.service.EmbeddingStore.IEmbeddingStore;
import com.fbytes.llmka.service.NewsDataCheck.INewsDataCheck;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.core.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
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
    @Autowired
    MeterRegistry meterRegistry;

    private static final Logger logger = Logger.getLogger(NewsDataCheck.class);

    private final ReadWriteLock metaHashCompressLock = new ReentrantReadWriteLock();
    private final AtomicInteger metaHashSeq = new AtomicInteger(0);   // reset on metaHash compress

    private Map<BigInteger, Pair<Integer, String>> metaHash = new ConcurrentHashMap<>();   // <MD5, <seq#, id>>

    @PostConstruct
    private void init(){
        Gauge.builder("llmka.newsdatacheck.metahash.size",fetchMetaHashSize()).register(meterRegistry);
    }

    private Supplier<Number> fetchMetaHashSize(){
        return () -> metaHash.size();
    }

    @Override
    @Timed(value="llmka.newsdatacheck.time",description="time to check news for duplicates",percentiles={0.5,0.9})
    public Optional<NewsCheckRejectReason> checkNewsData(EmbeddedData embeddedData) {
        if (!checkMeta(embeddedData))
            return Optional.of(new NewsCheckRejectReason(NewsCheckRejectReason.REASON.META_DUPLICATION, ""));
        Optional<List<Content>> result = embeddingStore.checkAndStore(embeddedData.getSegments(), embeddedData.getEmbeddings(), scoreLimit);
        if (!result.isEmpty())
            return Optional.of(new NewsCheckRejectReason(NewsCheckRejectReason.REASON.CLOSE_MATCH, result.get().get(0).textSegment().text()));
        return Optional.empty();
    }


    // check hash of meta for each segment
    @Timed(value="llmka.newsdatacheck.checkmeta.time",description="time to check meta for duplicates",percentiles={0.5,0.9})
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
            if (metaHash.size() > metaHashSizeLimit) {
                try {
                    metaHashCompressLock.writeLock().lock();
                    Pair<Map<BigInteger, Pair<Integer, String>>, List<String>> compressionResult = compressMetaHash(metaHashSizeCore);
                    metaHash = compressionResult.getLeft();
                    embeddingStore.removeIDes(compressionResult.getRight());
                } finally {
                    metaHashCompressLock.writeLock().unlock();
                }
            }
            return true;
        }
    }


    // remove all IDes, that are not in metaHash
    @Timed(value="llmka.newsdatacheck.cleanupstore.time",description="time to cleanup the store",percentiles={0.5,0.9})
    public void cleanupStore() {
        logger.info("cleanupStore. Current hash size: {}", metaHash.size());
        Set<String> idsSet = metaHash.entrySet().stream()
                .map(entry -> entry.getValue().getRight())
                .collect(Collectors.toSet());
        embeddingStore.removeOtherIDes(idsSet);
    }


    // returns <newHashMap, List<removedIDes>
    @Timed(value="llmka.newsdatacheck.compressMetaHash.time",description="time to compress metaHash",percentiles={0.5,0.9})
    private Pair<Map<BigInteger, Pair<Integer, String>>, List<String>> compressMetaHash(int reduceToSize) {
        logger.info("Compressing metaHash. Current size: {}", metaHash.size());
        Map<BigInteger, Pair<Integer, String>> newMetaMap = new ConcurrentHashMap();
        Map.Entry<BigInteger, Pair<Integer, String>>[] entriesArr = metaHash.entrySet().toArray(new Map.Entry[0]);
        Arrays.sort(entriesArr, Map.Entry.<BigInteger, Pair<Integer, String>>comparingByValue().reversed());

        metaHashSeq.set(0);
        ConcurrentHashMap<BigInteger, Pair<Integer, String>> newMetaHash = Arrays.stream(entriesArr, 0, reduceToSize)
                .collect(Collectors.toMap(entry -> entry.getKey(),
                        entry -> Pair.of(metaHashSeq.getAndIncrement(), entry.getValue().getRight()),    // replace seq#
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
