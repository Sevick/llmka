import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.impl.EmbeddingService;
import com.fbytes.llmka.service.EmbeddedStore.dao.IEmbeddedStore;
import com.fbytes.llmka.service.EmbeddedStore.dao.impl.EmbeddedStore;
import config.TestConfig;
import dev.langchain4j.rag.content.Content;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootTest(classes = {EmbeddingService.class, EmbeddedStoreTest.EmbeddedStoreTestConfig.class})
public class EmbeddedStoreTest {

    @Autowired
    private IEmbeddingService embeddingService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private IEmbeddedStore embeddedStore;

    @TestConfiguration
    static class EmbeddedStoreTestConfig {
        @Bean
        public IEmbeddedStore embeddedStore() {
            return new EmbeddedStore("testschema");
        }
    }


//    @PostConstruct
//    private void init() {
//        try {
//            embeddedStore = (IEmbeddedStore) context.getBean("testStoreBean");
//        }
//        catch (NoSuchBeanDefinitionException ex){
//            embeddedStore = new EmbeddedStore("testschema");
//            context.getAutowireCapableBeanFactory().autowireBean(embeddedStore);
//            context.getAutowireCapableBeanFactory().initializeBean(embeddedStore, "testStoreBean");
//            ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
//            configurableContext.getBeanFactory().registerSingleton("testStoreBean", embeddedStore);
//        }
//    }


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

    @Test
    public void testSecondaryInit(){
        Assert.isTrue(true, "failed");
    }

    private void removeTestData(){

    }
}