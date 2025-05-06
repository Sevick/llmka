package playground.real;

import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.config.newssource.RssNewsSource;
import com.fbytes.llmka.service.DataRetriver.IDataRetriever;
import com.fbytes.llmka.service.DataRetriver.impl.DataRetrieverRSS;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.EmbeddingService;
import com.fbytes.llmka.service.EmbeddedStore.IEmbeddedStoreService;
import com.fbytes.llmka.service.EmbeddedStore.EmbeddedStoreService;
import config.TestConfig;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.rag.content.Content;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Disabled
@SpringBootTest(classes = {EmbeddingService.class, EmbeddedStoreService.class, DataRetrieverRSS.class, RestTemplate.class})
@ContextConfiguration(classes = {TestConfig.class})
class FullFlowTest {

    @Autowired
    private IDataRetriever<RssNewsSource> rssRetriever;
    @Autowired
    private IEmbeddedStoreService embeddingStoreService;

    @Autowired
    private IEmbeddingService embeddingService;

    @Autowired
    private RestTemplate restTemplate;

    private String rssUrl = "https://detaly.co.il/feed/";
    private RssNewsSource rssDataSource = new RssNewsSource("DatasourceID", "RssRetriver", rssUrl, "GroupName");

    @Test
    void flowTest() {
        Optional<Stream<NewsData>> dataStream = rssRetriever.retrieveData(rssDataSource);
        dataStream.ifPresent(list -> list.forEach(newsData -> {
                    EmbeddedData embeddings = embeddingService.embedNewsData(newsData);
                    embeddingStoreService.store("testschema", embeddings.getSegments(), embeddings.getEmbeddings());
                }
        ));

        String query = "weather forecast";
        Embedding embeddings = embeddingService.embedStr(query);
        Optional<List<Content>> result = embeddingStoreService.retrieve("testschema", embeddings, 3, 0.5f);

        System.out.println(result);
    }
}
