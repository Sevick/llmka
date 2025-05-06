package unit;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.Embedding.IEmbeddingService;
import com.fbytes.llmka.service.Embedding.EmbeddingService;
import dev.langchain4j.store.embedding.CosineSimilarity;
import dev.langchain4j.store.embedding.RelevanceScore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.Optional;

@SpringBootTest(classes = {EmbeddingService.class})
class EmbeddingTest {
    private static final Logger logger = Logger.getLogger(EmbeddingTest.class);

    @Value("#{T(Float).parseFloat('${llmka.newscheck.datacheck.score_limit}')}")
    private Float scoreLimit;

    @Autowired
    private IEmbeddingService embeddingService;

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
        Assert.isTrue(score > scoreLimit, String.format("Similar texts got score of %f (<%f)", score, scoreLimit));
    }


    @Test
    void similarityTest2() {
        NewsData newsData1 = NewsData.builder()
                .id("xxx1yyy")
                .link("http://1/aaaaaa")
                .title("Академия иврита: иерусалимская бабочка будет теперь носить имя Ариэля Бибаса.")
                .description(Optional.of("5-летний \"рыжик\" любил бабочек, но особенно эту, похожую цветом на него самого."))
                .build();
        NewsData newsData2 = NewsData.builder()
                .id("yyy1xxx")
                .link("http://2/bbbbb")
                .title("Академия иврита назвала одну из бабочек в память о 5-летнем Ариэле Бибасе.")
                .description(Optional.of("Эта оранжевая бабочка в черных пятнах (по-русски ее называют \"шашечница изящная\") принадлежит к виду дневных бабочек рода Melitaea семейства Нимфалиды (Nymphalidae)."))
                .build();
        EmbeddedData embeddings1 = embeddingService.embedNewsData(newsData1);
        EmbeddedData embeddings2 = embeddingService.embedNewsData(newsData2);

        double cosineSimilarity = CosineSimilarity.between(embeddings1.getEmbeddings().get(0), embeddings2.getEmbeddings().get(0));
        double score = RelevanceScore.fromCosineSimilarity(cosineSimilarity);

        logger.info("similarityTest2 Score: {}", String.valueOf(score));
        Assert.isTrue(score > scoreLimit, MessageFormat.format("Similar texts got score of {0} (<{1})", score, scoreLimit));
    }

    @Test
    void similarityTest3() {
        NewsData newsData1 = NewsData.builder()
                .id("1")
                .link("http://1")
                .title("СМИ: Иран нанял киллера из Грузии для ликвидации раввина в Азербайджане.")
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

        logger.info("similarityTest3 Score: {}", String.valueOf(score));
        Assert.isTrue(score > scoreLimit, String.format("Similar texts got score of %f (<%f)", score, scoreLimit));
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

        logger.info("similarityTest4 Score: {}", String.valueOf(score));
        Assert.isTrue(score > scoreLimit, String.format("Similar texts got score of %f (<%f)", score, scoreLimit));
    }


    @Test
    void similarityTest5() {
        NewsData newsData1 = NewsData.builder()
                .id("1")
                .link("http://1")
                .title("Бывшая заложница Ноа Аргамани вошла в список 100 самых влиятельных людей мира по версии журнала TIME.")
                .description(Optional.of("Бывшая заложница ХАМАСа Ноя Аргемани, освобождённая из Газы в ходе операции «Арнон», признана одной из самых влиятельных персон года по версии американского журнала TIME. Текст о ней написал Даг Эмхофф — супруг экс-вице-президента США Камалы Харрис."))
                .build();
        NewsData newsData2 = NewsData.builder()
                .id("2")
                .link("http://2")
                .title("100 самых влиятельных людей 2025 года от Time: Трамп, Маск, Аргамани и другие.")
                .description(Optional.of("Журнал Time опубликовал список \"100 самых влиятельных людей 2025 года\". В списке израильтянка – Ноа Аргамани, похищенная террористами ХАМАСа во время бойни на фестивале Nova и освобожденная ЦАХАЛом."))
                .build();
        NewsData newsData3 = NewsData.builder()
                .id("2")
                .link("http://2")
                .title("Освобожденная заложница - в списке 100 самых влиятельных людей мира.")
                .description(Optional.of("Журнал Time поставил имя Ноа Аргамани в один ряд с Трампом, Маском, звездами кино и спорта."))
                .build();
        EmbeddedData embeddings1 = embeddingService.embedNewsData(newsData1);
        EmbeddedData embeddings2 = embeddingService.embedNewsData(newsData2);
        EmbeddedData embeddings3 = embeddingService.embedNewsData(newsData3);

        double cosineSimilarity21 = CosineSimilarity.between(embeddings1.getEmbeddings().get(0), embeddings2.getEmbeddings().get(0));
        double cosineSimilarity31 = CosineSimilarity.between(embeddings1.getEmbeddings().get(0), embeddings3.getEmbeddings().get(0));
        double cosineSimilarity32 = CosineSimilarity.between(embeddings2.getEmbeddings().get(0), embeddings3.getEmbeddings().get(0));

        double score21 = RelevanceScore.fromCosineSimilarity(cosineSimilarity21);
        double score31 = RelevanceScore.fromCosineSimilarity(cosineSimilarity31);
        double score32 = RelevanceScore.fromCosineSimilarity(cosineSimilarity32);

        logger.info("similarityTest5 Scores:\n2-1: {}, 3-1: {}, 3-2: {}", String.valueOf(score21), String.valueOf(score31), String.valueOf(score32));
        Assert.isTrue(score21 > scoreLimit, String.format("Similar texts got score21 of %f (<%f)", score21, scoreLimit));
    }
}