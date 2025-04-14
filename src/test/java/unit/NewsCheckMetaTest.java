package unit;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.impl.EmbeddingService;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import com.fbytes.llmka.service.NewsCheck.impl.NewsCheckMeta;
import config.TestConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SpringBootTest(classes = {EmbeddingService.class, NewsCheckMeta.class})
@ContextConfiguration(classes = {TestConfig.class})
class NewsCheckMetaTest {

    @Autowired
    private NewsCheckMeta newsCheckMeta;
    @Autowired
    private IEmbeddingService embeddingService;


    @Test
    void metaHashCompressionTest() {
        final int targetSize = 5;

        Map<BigInteger, Pair<Integer, String>> testMetaHash = new ConcurrentHashMap<>();
        for (int i = 100; i < 120; i++) {
            testMetaHash.put(BigInteger.valueOf(i), Pair.of(i, String.valueOf(i)));
        }

        Field metaHash = null;
        Method method = null;
        Pair<Map<BigInteger, Pair<Integer, String>>, List<String>> compressResult;
        try {
            metaHash = NewsCheckMeta.class.getDeclaredField("metaHash");
            metaHash.setAccessible(true);
            method = newsCheckMeta.getClass().getDeclaredMethod("compressMetaHash", int.class);
            method.setAccessible(true);

            metaHash.set(newsCheckMeta, testMetaHash);
            compressResult = (Pair<Map<BigInteger, Pair<Integer, String>>, List<String>>) method.invoke(newsCheckMeta, targetSize);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get access to privates of NewsDataCheck: " + e.getMessage());
        } finally {
            if (metaHash != null)
                metaHash.setAccessible(false);
            if (method != null)
                method.setAccessible(false);
        }

        Assert.isTrue(compressResult != null, "compressResult is null");
        Map<BigInteger, Pair<Integer, String>> newHashMap = compressResult.getLeft();
        List<String> removedIDs = compressResult.getRight();

        Assert.isTrue(newHashMap.size() == targetSize, "compress returned new hashMap of unexpected size");

        Set<String> restIds = newHashMap.values().stream().map(item -> item.getRight()).collect(Collectors.toSet());
        removedIDs.forEach(id -> Assert.isTrue(!restIds.contains(id), "ID from removeID list found in new map: " + id));
    }


    @Test
    void newsCheckMetaDuplicationTest() {
        NewsData newsData = NewsData.builder()
                .id("ID1")
                .dataSourceID("DataSourceID")
                .link("http://somelink")
                .title("Title")
                .description(Optional.of("Description"))
                .text(Optional.empty())
                .build();

        newsCheckMeta.checkNews("testschema", newsData);

        Optional<INewsCheck.RejectReason> duplication = newsCheckMeta.checkNews("testschema", newsData);
        Assert.isTrue(!duplication.isEmpty(), "Duplication not detected");
        Assert.isTrue(duplication.get().getReason() == INewsCheck.RejectReason.REASON.META_DUPLICATION, "Record rejected, but reject reason is not META_DUPLICATION");
    }
}
