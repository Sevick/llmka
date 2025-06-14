package com.fbytes.llmka.service.NewsCheck.impl;

import com.fbytes.llmka.config.profiles.metrics.ParamTimedMetric;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.appevent.AppEventMetahashCompress;
import com.fbytes.llmka.service.Maintenance.AppEventSenderService.IAppEventSenderService;
import com.fbytes.llmka.service.INewsIDStore;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class NewsCheckMetaSchema implements INewsCheck, INewsIDStore {
    private static final Logger logger = Logger.getLogger(NewsCheckMetaSchema.class);

    @Value("${llmka.newscheck.metacheck.metahash_size_limit:64}")
    private Integer metaHashSizeLimit;
    @Value("${llmka.newscheck.metacheck.metahash_size_core:32}")
    private Integer metaHashSizeCore;

    @Autowired
    private MeterRegistry meterRegistry;
    @Autowired
    IAppEventSenderService appEventSenderService;

    private final String schema;
    // Lock used to block hashMap for compressions, so compression accuires write lock and usual map manipulations - read lock
    private final ReadWriteLock metaHashCompressLock = new ReentrantReadWriteLock();
    // used to track order of elements, reset on metaHash compress (existing elements re-enumerated)
    private final AtomicInteger metaHashSeq = new AtomicInteger(0);

    private ConcurrentMap<BigInteger, Pair<Integer, String>> metaHash = new ConcurrentHashMap<>();   // <MD5, <seq#, id>>

    // keep ref to prevent GC (weak ref)
    private Gauge metaSizeGauge;


    public NewsCheckMetaSchema(String schema) {
        this.schema = schema;
    }

    @PostConstruct
    private void init() {
        if (meterRegistry != null) {
            metaSizeGauge = Gauge.builder("llmka.newsmetacheck.metahash.size", fetchMetaHashSize())
                    .tag("schema", schema)
                    .register(meterRegistry);
        }
    }

    private Supplier<Number> fetchMetaHashSize() {
        return () -> metaHash.size();
    }


    private String extractMeta(NewsData newsData) {
        String metaStr = newsData.getExtID();
        metaStr = (metaStr == null || metaStr.isEmpty()) ? newsData.getTitle() : metaStr;
        return metaStr;
    }

    @Override
    @ParamTimedMetric(key = "schema")
    public Optional<RejectReason> checkNews(String schema, NewsData newsData) {
        String metaStr = extractMeta(newsData);
        Pair<Integer, String> result;
        try {
            metaHashCompressLock.readLock().lock();
            result = metaHash.putIfAbsent(calculateMD5Hash(metaStr), Pair.of(metaHashSeq.getAndIncrement(), newsData.getExtID()));
        } finally {
            metaHashCompressLock.readLock().unlock();
        }

        if (result != null) {
            logger.trace("NewsCheckMeta filtered: {}", newsData.toText());
            return Optional.of(new RejectReason(RejectReason.REASON.META_DUPLICATION));
        } else {
            if (metaHash.size() > metaHashSizeLimit) {
                logger.trace("Lock acquired for meta compression");
                compressMetaHash(metaHashSizeCore);
            }
            return Optional.empty();
        }
    }

    // returns <newHashMap, List<removedIDes>
    @Timed(value = "llmka.newsdatacheck.compressmetahash.time", description = "time to compress metaHash", percentiles = {0.5, 0.9})
    // TODO: Add metric manually - annotations are not working on private method invokations
    // method is tested using reflections (method name)
    public Pair<ConcurrentMap<BigInteger, Pair<Integer, String>>, List<String>> compressMetaHash(Integer targetSize) {
        if (targetSize == null)
            targetSize = metaHashSizeCore;
        if (targetSize >= metaHash.size())
            return Pair.of(metaHash, new ArrayList<>());
        Map.Entry<BigInteger, Pair<Integer, String>>[] entriesArr;
        ConcurrentHashMap<BigInteger, Pair<Integer, String>> newMetaHash;
        try {
            logger.trace("[{}] Acquiring write lock for meta compression", schema);
            metaHashCompressLock.writeLock().lock();
            logger.trace("[{}] Lock acquired for meta compression", schema);

            logger.info("[{}] Compressing metaHash. Current size: {}", schema, metaHash.size());
            entriesArr = metaHash.entrySet().toArray(new Map.Entry[0]);
            Arrays.sort(entriesArr, Map.Entry.<BigInteger, Pair<Integer, String>>comparingByValue());   // ascending order by seq#

            metaHashSeq.set(0);
            newMetaHash = Arrays.stream(entriesArr, entriesArr.length - targetSize, entriesArr.length)
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> Pair.of(metaHashSeq.getAndIncrement(), entry.getValue().getRight()),    // replace seq#
                            (existing, replacement) -> existing,
                            ConcurrentHashMap::new));
            metaHash = newMetaHash;
        } finally {
            metaHashCompressLock.writeLock().unlock();
            logger.trace("[{}] Meta compression lock released", schema);
        }
        List<String> removedIdList = Arrays.stream(entriesArr, targetSize + 1, entriesArr.length - 1)
                .map(entry -> entry.getValue().getRight())
                .toList();
        appEventSenderService.sendEvent(new AppEventMetahashCompress("NewsCheckMetaSchema", schema));
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

    @Override
    public Set<String> fetchIDList(String schema) {
        Set<String> idsSet = metaHash.entrySet().stream()
                .map(entry -> entry.getValue().getRight())
                .collect(Collectors.toSet());
        return idsSet;
    }
}
