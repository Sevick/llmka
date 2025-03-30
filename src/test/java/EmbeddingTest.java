import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.EmbeddedStore.dao.IEmbeddedStore;
import com.fbytes.llmka.service.EmbeddedStore.dao.impl.EmbeddedStore;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.impl.EmbeddingService;
import dev.langchain4j.rag.content.Content;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootTest(classes = {EmbeddingService.class})
public class EmbeddingTest {

    @Autowired
    private IEmbeddingService embeddingService;

    @Autowired
    private ApplicationContext context;

    private IEmbeddedStore embeddedStore;

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

        EmbeddedData embeddedData = embeddingService.embedNewsData(newsData);
        Assert.isTrue(!embeddedData.getSegments().isEmpty(), "Segments are missing in embedded data");
        Assert.isTrue(!embeddedData.getEmbeddings().isEmpty(), "Embeddings are missing in embedded data");
    }

}