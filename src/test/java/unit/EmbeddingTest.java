package unit;

import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.EmbeddedStore.dao.IEmbeddedStore;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.impl.EmbeddingService;
import dev.langchain4j.store.embedding.CosineSimilarity;
import dev.langchain4j.store.embedding.RelevanceScore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.Optional;

@SpringBootTest(classes = {EmbeddingService.class})
class EmbeddingTest {

    private static final float SCORE_LIMIT = 0.82f;

    @Autowired
    private IEmbeddingService embeddingService;

    private IEmbeddedStore embeddedStore;

    @Test
    void testEmbeddingService() {
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
    void similarityTest1() {
        NewsData newsData1 = NewsData.builder()
                .id("1")
                .link("http://1")
                .title("В Явне в результате ДТП тяжело травмирован пешеход.")
                .description(Optional.of("На улице А-Дугит в Явне автомобиль сбил пешехода. Парамедики службы скорой помощи \"Маген Давид Адом\" передали, что пострадавший (примерно 40 лет) при падении ударился головой и получил черепно-мозговую травму."))
                .build();
        NewsData newsData2 = NewsData.builder()
                .id("2")
                .link("http://2")
                .title("Машина сбила 40-летнего мужчину в Явне, он в тяжелом состоянии. Автомобиль сбил мужчину в возрасте около 40 лет в Явне.")
                .description(Optional.empty())
                .build();
        EmbeddedData embeddings1 = embeddingService.embedNewsData(newsData1);
        EmbeddedData embeddings2 = embeddingService.embedNewsData(newsData2);

        double cosineSimilarity = CosineSimilarity.between(embeddings1.getEmbeddings().get(0), embeddings2.getEmbeddings().get(0));
        double score = RelevanceScore.fromCosineSimilarity(cosineSimilarity);

        System.out.println("similarityTest1 Score: " + String.valueOf(score));
        Assert.isTrue(score > SCORE_LIMIT, String.format("Similar texts got score of %f (<%f)", score, SCORE_LIMIT));
    }


    @Test
    void similarityTest2() {
        NewsData newsData1 = NewsData.builder()
                .id("1")
                .link("http://1")
                .title("Академия иврита: иерусалимская бабочка будет теперь носить имя Ариэля Бибаса.")
                .description(Optional.of("5-летний \"рыжик\" любил бабочек, но особенно эту, похожую цветом на него самого."))
                .build();
        NewsData newsData2 = NewsData.builder()
                .id("2")
                .link("http://2")
                .title("Академия иврита назвала одну из бабочек в память о 5-летнем Ариэле Бибасе.")
                .description(Optional.of("Эта оранжевая бабочка в черных пятнах (по-русски ее называют \"шашечница изящная\") принадлежит к виду дневных бабочек рода Melitaea семейства Нимфалиды (Nymphalidae)."))
                .build();
        EmbeddedData embeddings1 = embeddingService.embedNewsData(newsData1);
        EmbeddedData embeddings2 = embeddingService.embedNewsData(newsData2);

        double cosineSimilarity = CosineSimilarity.between(embeddings1.getEmbeddings().get(0), embeddings2.getEmbeddings().get(0));
        double score = RelevanceScore.fromCosineSimilarity(cosineSimilarity);

        System.out.println("similarityTest2 Score: " + String.valueOf(score));
        Assert.isTrue(score > SCORE_LIMIT, MessageFormat.format("Similar texts got score of {0} (<{1})", score, SCORE_LIMIT));
    }

    @Test
    void similarityTest3() {
        NewsData newsData1 = NewsData.builder()
                .id("1")
                .link("http://1")
                .title("СМИ: Иран нанял киллера из Грузии для ликвидации раввина в Азербайджане .")
                .description(Optional.of("О попытке покушения стало известно в начале 2025 года, но его цель была раскрыта только сейчас."))
                .build();
        NewsData newsData2 = NewsData.builder()
                .id("2")
                .link("http://2")
                .title("Иран заказал убийство раввина в Азербайджане.")
                .description(Optional.of("Иранские силы «Кудс» наняли наркоторговца, чтобы убить азербайджанского раввина. Преступление предотвратили еще в январе, но его."))
                .build();
        EmbeddedData embeddings1 = embeddingService.embedNewsData(newsData1);
        EmbeddedData embeddings2 = embeddingService.embedNewsData(newsData2);

        double cosineSimilarity = CosineSimilarity.between(embeddings1.getEmbeddings().get(0), embeddings2.getEmbeddings().get(0));
        double score = RelevanceScore.fromCosineSimilarity(cosineSimilarity);

        System.out.println("similarityTest3 Score: " + String.valueOf(score));
        //Assert.isTrue(score > scoreLimit, String.format("Similar texts got score of %f (<%f)", score, scoreLimit));
    }

    @Test
    void similarityTest4() {
        NewsData newsData1 = NewsData.builder()
                .id("1")
                .link("http://1")
                .title("СМИ: Иран нанял киллера из Грузии для ликвидации раввина в Азербайджане .")
                .description(Optional.empty())
                .build();
        NewsData newsData2 = NewsData.builder()
                .id("2")
                .link("http://2")
                .title("Иран заказал убийство раввина в Азербайджане.")
                .description(Optional.empty())
                .build();
        EmbeddedData embeddings1 = embeddingService.embedNewsData(newsData1);
        EmbeddedData embeddings2 = embeddingService.embedNewsData(newsData2);

        double cosineSimilarity = CosineSimilarity.between(embeddings1.getEmbeddings().get(0), embeddings2.getEmbeddings().get(0));
        double score = RelevanceScore.fromCosineSimilarity(cosineSimilarity);

        System.out.println("similarityTest4 Score: " + String.valueOf(score));
        //Assert.isTrue(score > scoreLimit, String.format("Similar texts got score of %f (<%f)", score, scoreLimit));
    }
}