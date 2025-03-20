import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.impl.EmbeddingService;
import com.fbytes.llmka.service.EmbeddingStore.IEmbeddedStore;
import com.fbytes.llmka.service.EmbeddingStore.impl.EmbeddedStore;
import dev.langchain4j.rag.content.Content;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootTest(classes = {EmbeddingService.class})
public class EmbeddedStoreTest {

    @Autowired
    private IEmbeddingService embeddingService;

    @Autowired
    private ApplicationContext context;


    private IEmbeddedStore embeddedStore;


    @BeforeEach
    void setupBeforeAll() {
        embeddedStore = new EmbeddedStore("testschema");
        context.getAutowireCapableBeanFactory().autowireBean(embeddedStore);
        context.getAutowireCapableBeanFactory().initializeBean(embeddedStore, "testStoreBean");
    }


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


    @Test
    public void similarityTest() {
        NewsData newsData1 = NewsData.builder()
                .id("1")
                .link("http://1")
                .title("Минэнерго: В марте цена на бензин снизится на 8 агор.")
                .description(Optional.of("27 февраля министерство энергетики объявило, что в ночь с 1 на 2 марта цена на бензин снизится на 8 агор. Согласно объявлению, цена на 95-октановый."))
                .build();

        NewsData newsData2 = NewsData.builder()
                .id("2")
                .link("http://2")
                .title("Министерство энергетики сообщило, что в ночь на воскресенье, 2 марта, стоимость литра бензина с октановым числом 95 в Израиле снизится на 8 агорот до 7,23 шекеля при самообслуживании. ")
                .description(Optional.of("2 марта бензин подешевеет на 8 агорот за литр."))
                .build();

        NewsData newsData3 = NewsData.builder()
                .id("3")
                .link("http://3")
                .title("Внезапно: в начале  марта снизится цена на бензин.")
                .description(Optional.of("В полночь на воскресенье несколько понизится цена на топливо. Об этом сделало объявление министерство энергетики."))
                .build();

        Optional<Stream<NewsData>> dataStream = Optional.of(Arrays.asList(newsData1, newsData2).stream());
        dataStream.ifPresent(list -> list.forEach(newsData -> {
                    EmbeddedData embeddings = embeddingService.embedNewsData(newsData);
                    embeddedStore.store(embeddings.getSegments(), embeddings.getEmbeddings());
                }
        ));

        EmbeddedData embeddings = embeddingService.embedNewsData(newsData3);
        Optional<List<Content>> result = embeddedStore.retrieve(embeddings.getEmbeddings(), 3, 0.85f);

        Assert.isTrue(!result.isEmpty() && result.get().size() > 0, "No similar news found");
    }
}