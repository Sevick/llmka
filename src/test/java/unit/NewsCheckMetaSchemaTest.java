package unit;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.appevent.AppEvent;
import com.fbytes.llmka.model.appevent.AppEventMetahashCompress;
import com.fbytes.llmka.service.Maintenance.AppEventSenderService.IAppEventSenderService;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import com.fbytes.llmka.service.NewsCheck.impl.ISchemaMetaServiceFactory;
import com.fbytes.llmka.service.NewsCheck.impl.NewsCheckMetaSchema;
import com.fbytes.llmka.service.NewsCheck.impl.SchemaMetaServiceFactory;
import config.TestConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig(classes = {TestConfig.class, SchemaMetaServiceFactory.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NewsCheckMetaSchemaTest {
    private static final Logger logger = Logger.getLogger(NewsCheckMetaSchemaTest.class);

    @MockitoBean
    IAppEventSenderService<AppEvent> appEventSender;

    @Autowired
    ISchemaMetaServiceFactory schemaMetaServiceFactory;

    private NewsCheckMetaSchema newsCheckMetaSchema;

    @BeforeAll
    void init() {
        newsCheckMetaSchema = schemaMetaServiceFactory.createSchemaMetaService("testschema");
    }

    @Test
    void metaHashCompressionTest() {
        NewsCheckMetaSchema newsCheckMetaSchema = schemaMetaServiceFactory.createSchemaMetaService("testschema");

        final int targetSize = 5;
        Map<BigInteger, Pair<Integer, String>> testMetaHash = new ConcurrentHashMap<>();
        for (int i = 100; i < 120; i++) {
            testMetaHash.put(BigInteger.valueOf(i), Pair.of(i, String.valueOf(i)));
        }

        Field metaHash = null;
        Method method = null;
        Pair<ConcurrentMap<BigInteger, Pair<Integer, String>>, List<String>> compressResult;
        try {
            metaHash = NewsCheckMetaSchema.class.getDeclaredField("metaHash");
            metaHash.setAccessible(true);
            method = newsCheckMetaSchema.getClass().getDeclaredMethod("compressMetaHash", Integer.class);
            method.setAccessible(true);

            metaHash.set(newsCheckMetaSchema, testMetaHash);
            compressResult = (Pair<ConcurrentMap<BigInteger, Pair<Integer, String>>, List<String>>) method.invoke(newsCheckMetaSchema, targetSize);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get access to privates of NewsDataCheck: ", e);
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

        verify(appEventSender, times(1)).sendEvent(any(AppEventMetahashCompress.class));
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

        newsCheckMetaSchema.checkNews("testschema", newsData);

        Optional<INewsCheck.RejectReason> duplication = newsCheckMetaSchema.checkNews("testschema", newsData);
        Assert.isTrue(!duplication.isEmpty(), "Duplication not detected");
        Assert.isTrue(duplication.get().getReason() == INewsCheck.RejectReason.REASON.META_DUPLICATION, "Record rejected, but reject reason is not META_DUPLICATION");
    }
}
