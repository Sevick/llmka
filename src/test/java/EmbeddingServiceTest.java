import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.impl.EmbeddingService;
import com.fbytes.llmka.service.EmbeddingStore.impl.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.Optional;

@SpringBootTest(classes = {EmbeddingService.class, EmbeddingStore.class})
public class EmbeddingServiceTest {

    @Autowired
    private IEmbeddingService embeddingService;

    @Test
    public void testEmbeddingService() {

        NewsData newsData = NewsData.builder()
                .id("ID1")
                .dataSourceID("DataSourceID")
                .link("http://somelink")
                .title("Title")
                .description(Optional.of("Description"))
                .text(Optional.empty())
                .build();

        embeddingService.embedNewsData(newsData);
        Assert.isTrue(true, "Embedding failed");
    }
}
