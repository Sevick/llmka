import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsCheckRejectReason;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.EmbeddedStore.impl.EmbeddedStoreService;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.impl.EmbeddingService;
import com.fbytes.llmka.service.NewsCheck.impl.NewsMetaCheck;
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

@SpringBootTest(classes = {EmbeddingService.class, NewsMetaCheck.class})
@ContextConfiguration(classes = {TestConfig.class})
public class NewsMetaCheckTest {

    @Autowired
    private NewsMetaCheck newsMetaCheck;
    @Autowired
    private IEmbeddingService embeddingService;


    @Test
    public void metaHashCompressionTest() {
        final int targetSize = 5;

        Map<BigInteger, Pair<Integer, String>> testMetaHash = new ConcurrentHashMap<>();
        for (int i = 100; i < 120; i++) {
            testMetaHash.put(BigInteger.valueOf(i), Pair.of(i, String.valueOf(i)));
        }

        Field metaHash = null;
        Method method = null;
        Pair<Map<BigInteger, Pair<Integer, String>>, List<String>> compressResult;
        try {
            metaHash = NewsMetaCheck.class.getDeclaredField("metaHash");
            metaHash.setAccessible(true);
            method = newsMetaCheck.getClass().getDeclaredMethod("compressMetaHash", int.class);
            method.setAccessible(true);

            metaHash.set(newsMetaCheck, testMetaHash);
            compressResult = (Pair<Map<BigInteger, Pair<Integer, String>>, List<String>>) method.invoke(newsMetaCheck, targetSize);
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
    public void newsCheckMetaDuplicationTest() {
        NewsData newsData = NewsData.builder()
                .id("ID1")
                .dataSourceID("DataSourceID")
                .link("http://somelink")
                .title("Title")
                .description(Optional.of("Description"))
                .text(Optional.empty())
                .build();

        EmbeddedData embeddedNewsData = embeddingService.embedNewsData(newsData);
        newsMetaCheck.checkNews("testschema", embeddedNewsData);

        Optional<NewsCheckRejectReason> duplication = newsMetaCheck.checkNews("testschema", embeddedNewsData);
        Assert.isTrue(!duplication.isEmpty(), "Duplication not detected");
        Assert.isTrue(duplication.get().getReason() == NewsCheckRejectReason.REASON.META_DUPLICATION, "Record rejected, but reject reason is not META_DUPLICATION");
    }
}
