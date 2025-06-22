package integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.EmbeddedStore.EmbeddedStoreService;
import com.fbytes.llmka.service.EmbeddedStore.SchemaStoreFactory;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.EmbeddingService;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import com.fbytes.llmka.service.NewsCheck.impl.NewsCheckData;
import config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import java.util.Optional;

@SpringBootTest(classes = {EmbeddingService.class, NewsCheckData.class, EmbeddedStoreService.class, SchemaStoreFactory.class})
@ContextConfiguration(classes = {TestConfig.class})
class NewsCheckDataTest {
    private static final Logger logger = Logger.getLogger(NewsCheckDataTest.class);
    
    @Autowired
    private NewsCheckData newsCheckDataService;
    @Autowired
    private IEmbeddingService embeddingService;

    @Test
    void newsCheckDataDuplicationTest() {

        NewsData newsData = NewsData.builder()
                .id("intID1")
                .extID("extID1223")
                .dataSourceID("DataSourceID")
                .link("http://somelink")
                .title("Title")
                .description(Optional.of("Description"))
                .text(Optional.empty())
                .build();

        Optional<INewsCheck.RejectReason> duplication;
        duplication = newsCheckDataService.checkNews("testschema", newsData);
        Assert.isTrue(duplication.isEmpty(), "Wrong duplication detectiond");

        duplication = newsCheckDataService.checkNews("testschema", newsData);
        Assert.isTrue(!duplication.isEmpty(), "Duplication not detected");
        Assert.isTrue(duplication.get().getReason() == INewsCheck.RejectReason.REASON.CLOSE_MATCH, "Record rejected, but reject reason is not CLOSE_MATCH");
    }
}
