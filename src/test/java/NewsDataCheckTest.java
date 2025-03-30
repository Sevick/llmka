import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsCheckRejectReason;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.impl.EmbeddingService;
import com.fbytes.llmka.service.EmbeddedStore.impl.EmbeddedStoreService;
import com.fbytes.llmka.service.NewsCheck.impl.NewsDataCheck;
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

@SpringBootTest(classes = {EmbeddingService.class, NewsDataCheck.class, EmbeddedStoreService.class, NewsMetaCheck.class})
@ContextConfiguration(classes = {TestConfig.class})
public class NewsDataCheckTest {

    @Autowired
    private NewsDataCheck newsDataCheckService;
    @Autowired
    private IEmbeddingService embeddingService;

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
        newsDataCheckService.checkNews("testschema", embeddedNewsData);

        Optional<NewsCheckRejectReason> duplication = newsDataCheckService.checkNews("testschema", embeddedNewsData);
        Assert.isTrue(!duplication.isEmpty(), "Duplication not detected");
        Assert.isTrue(duplication.get().getReason() == NewsCheckRejectReason.REASON.META_DUPLICATION, "Record rejected, but reject reason is not META_DUPLICATION");
    }
}
