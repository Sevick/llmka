package integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.DataRetriver.ParserRSS.ParserRSS;
import com.fbytes.llmka.service.LLMProvider.impl.LLMProviderLocalOllama;
import com.fbytes.llmka.service.LLMService.LLMService;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import com.fbytes.llmka.service.NewsProcessor.INewsProcessor;
import com.fbytes.llmka.service.NewsProcessor.impl.NewsProcessorLastSentence;
import config.TestConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.stream.Stream;

@Disabled
@ActiveProfiles("integration")
@SpringBootTest(classes = {LLMProviderLocalOllama.class, LLMService.class, NewsProcessorLastSentence.class, ParserRSS.class})
@ContextConfiguration(classes = {TestConfig.class})
public class LastSentenceTest {
    private static final Logger logger = Logger.getLogger(LastSentenceTest.class);

    @Autowired
    INewsProcessor lastSentenceProcessor;

    @ParameterizedTest
    @MethodSource("newsDataProvider")
    void testLastSentence_cutTail(Pair<String, String> input) {
        NewsData newsData = NewsData.builder()
                .id("LastSentenceTest#")
                .link("http://LastSentenceTest.somelink")
                .title(input.getLeft())
                .description(Optional.of(input.getRight()))
                .build();
        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentenceTest1: Last sentence result: {}", result);
        Assert.isTrue(!result.getDescription().isEmpty(), "New description is empty");
        Assert.isTrue(!result.getDescription().get().equals(newsData.getDescription().get()), "New description is equal old one");
        Assert.isTrue(result.isRewritten(), "Rewritten flag is not set");
        //Assert.isTrue(newsData.getDescription().get().startsWith(result.getDescription().get()), "New description is not a part of old one");
    }

    // <String, String> - <title, description>
    static Stream<Pair<String, String>> newsDataProvider() {
        return Stream.of(
                Pair.of("В Бат-Яме задержали мужчину с заряженным пистолетом.",
                        "Полиция сообщила, что в рамках борьбы с криминалом в Бат-Яме был задержан мужчина (21) с заряженным пистолетом калибра 9 мм. Оперативники из ")
        );
    }


    @Test
    void lastSentenceTest2() {
        NewsData newsData = NewsData.builder()
                .id("lastSentenceTest2")
                .link("http://dddd.llll.zz")
                .title("В Бат-Яме задержали мужчину с заряженным пистолетом.")
                .description(Optional.of("Оперативники из "))
                .build();

        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentenceTest2: Last sentence result: {}", result);
        Assert.isTrue(result.getDescription().isEmpty(), "Meaningless last sentence was not removed");
    }


    @Test
    void lastSentenceTest3() {
        NewsData newsData = NewsData.builder()
                .id("lastSentenceTest3")
                .link("http://dddd.llll.zz")
                .title("Спецпосланник Бёлер: Боевые действия в Газе прекратятся с возвращением всех заложников.")
                .description(Optional.of("Адам Бёлер, недавно назначенный президентом США Дональдом Трампом специальным посланником по реагированию на захват заложников, заявил"))
                .build();

        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentenceTest3: Last sentence result: {}", result);
        Assert.isTrue(result.getDescription().isEmpty(), "Meaningless last sentence was not removed");
    }

    @Test
    void lastSentenceTest4() {
        NewsData newsData = NewsData.builder()
                .id("lastSentenceTest4")
                .link("http://dddd.llll.zz")
                .title("В Бат-Яме задержали мужчину с заряженным пистолетом.")
                .description(Optional.of("Полиция сообщила, что в рамках борьбы с криминалом в Бат-Яме был задержан мужчина (21) с заряженным пистолетом калибра 9 мм."))
                .build();

        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentenceTest4: Last sentence result: {}", result);
        Assert.isTrue(!result.getDescription().isEmpty(), "New description is empty");
        Assert.isTrue(result.getDescription().get().equals(newsData.getDescription().get()), "New description is not equal old one");
        Assert.isTrue(!result.isRewritten(), "Rewritten flag is set, while content was not changed");
    }

    @Test
    void lastSentenceTest5() {
        NewsData newsData = NewsData.builder()
                .id("lastSentenceTest5")
                .link("http://dddd.llll.zz")
                .title("\"Скрипачка из Иерусалима\" обнаружена живой, но пропала еще одна девочка - из Ашдода")
                .description(Optional.of("После 10 дней поисков Ципора Александр была найдена, но теперь полиция опасается за другую пропавшую девочку, которой всего 15 лет"))
                .build();

        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentenceTest5: Last sentence result: {}", result);
        Assert.isTrue(!result.getDescription().isEmpty(), "New description is empty");
    }

    @Test
    void lastSentenceTest6() {
        NewsData newsData = NewsData.builder()
                .id("lastSentenceTest6")
                .link("http://dddd.llll.zz")
                .title("Умер раввин Мазуз, считавший репатриантов \"безбожниками\"")
                .description(Optional.of("Раввин Меир Мазуз скончался в возрасте 80 лет. Он считался одним из самых известных толкователей Галахи у сефардских евреев, в прошлом входил в Совет мудрецов Торы (высший орган партии ШАС), но потом покинул его, публично поддержав Эли Ишая. Впоследствии Мазуз вернулся в ШАС. Он регулярно обрушивался с нападками на русскоязычных репатриантов, считая их \"безбожниками\"."))
                .build();

        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentenceTest6: Last sentence result: {}", result);
        Assert.isTrue(!result.getDescription().isEmpty(), "New description is empty");
    }

    @Test
    void lastSentenceTest7() {
        NewsData newsData = NewsData.builder()
                .id("lastSentenceTest7")
                .link("http://dddd.llll.zz")
                .title("Cколько будет зарабатывать в хайтеке бакалавр?")
                .description(Optional.of("Рекрутинговая компания GotFriends подводит итоги первого квартала 2025 года на рынке высоких технологий и сообщает о росте средней зарплаты на 3% и "))
                .build();

        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentenceTest7: Last sentence result: {}", result);
        Assert.isTrue(!result.getDescription().isEmpty(), "New description is empty");
    }

    @Test
    void lastSentenceTest8() {
        NewsData newsData = NewsData.builder()
                .id("lastSentenceTest8")
                .link("http://dddd.llll.zz")
                .title("Anduril is working on the difficult AI-related task of real-time edge computing.")
                .description(Optional.of("Anduril announced its ninth acquisition on Monday with the purchase of Dublin’s Klas, makers of ruggedized edge computing equipment for the military and first-responders. Anduril wouldn’t reveal financial details of the deal, and the purchase is subject to regulatory approval, but the company did say that Klas employs 150 people. Relatedly, on Monday, Anduril also"))
                .build();

        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentenceTest8: Last sentence result: {}", result);
        Assert.isTrue(!result.getDescription().isEmpty(), "New description is empty");
        Assert.isTrue(result.getDescription().get().equals("Anduril announced its ninth acquisition on Monday with the purchase of Dublin’s Klas, makers of ruggedized edge computing equipment for the military and first-responders. Anduril wouldn’t reveal financial details of the deal, and the purchase is subject to regulatory approval, but the company did say that Klas employs 150 people."), "LastSentece failed to cut last useless sentence: " + result.getDescription().get());
    }

    @Test
    void lastSentenceTest9() {
        NewsData newsData = NewsData.builder()
                .id("lastSentenceTest9")
                .link("http://dddd.llll.zz")
                .title("Палестинский поэт получил «Пулитцера» за эссе о Газе.")
                .description(Optional.of("Поэт и основатель первой англоязычной библиотеки в Газе Мусаб Абу Тоха был удостоен Пулитцеровской премии за публицистические эссе. В них он "))
                .build();

        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentenceTest9: Last sentence result: {}", result);
        Assert.isTrue(!result.getDescription().isEmpty(), "New description is empty");
        Assert.isTrue(result.getDescription().get().equals("Поэт и основатель первой англоязычной библиотеки в Газе Мусаб Абу Тоха был удостоен Пулитцеровской премии за публицистические эссе."), "LastSentece failed to cut last useless sentence: " + result.getDescription().get());
    }
}
