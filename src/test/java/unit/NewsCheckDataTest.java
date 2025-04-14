package unit;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.EmbeddedStore.impl.EmbeddedStoreService;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.impl.EmbeddingService;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import com.fbytes.llmka.service.NewsCheck.impl.NewsCheckData;
import com.fbytes.llmka.service.NewsCheck.impl.NewsCheckMeta;
import config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import java.util.Optional;

@SpringBootTest(classes = {EmbeddingService.class, NewsCheckData.class, EmbeddedStoreService.class, NewsCheckMeta.class})
@ContextConfiguration(classes = {TestConfig.class})
class NewsCheckDataTest {

    @Autowired
    private NewsCheckData newsCheckDataService;
    @Autowired
    private IEmbeddingService embeddingService;

    @Test
    void newsCheckDataDuplicationTest() {
        NewsData newsData = NewsData.builder()
                .id("ID1")
                .dataSourceID("DataSourceID")
                .link("http://somelink")
                .title("Title")
                .description(Optional.of("Description"))
                .text(Optional.empty())
                .build();

        newsCheckDataService.checkNews("testschema", newsData);

        Optional<INewsCheck.RejectReason> duplication = newsCheckDataService.checkNews("testschema", newsData);
        Assert.isTrue(!duplication.isEmpty(), "Duplication not detected");
        Assert.isTrue(duplication.get().getReason() == INewsCheck.RejectReason.REASON.CLOSE_MATCH, "Record rejected, but reject reason is not CLOSE_MATCH");
    }
}
