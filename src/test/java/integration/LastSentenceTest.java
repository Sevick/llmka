package integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.LLMProvider.impl.LLMProviderLocalOllama;
import com.fbytes.llmka.service.LLMService.LLMService;
import com.fbytes.llmka.service.NewsProcessor.INewsProcessor;
import com.fbytes.llmka.service.NewsProcessor.impl.LastSentence.NewsProcessorLastSentence;
import config.TestConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(expression = "#{environment.acceptsProfiles('integration')}", reason = "Runs only for integration profile")
@SpringBootTest(classes = {NewsProcessorLastSentence.class, LLMService.class, LLMProviderLocalOllama.class})
@ContextConfiguration(classes = {TestConfig.class})
class LastSentenceTest {
    private static final Logger logger = Logger.getLogger(LastSentenceTest.class);

    @Autowired
    INewsProcessor lastSentenceProcessor;

    @ParameterizedTest
    @MethodSource("tailCutProvider")
    void testLastSentence_cutTail(Pair<String, String> input) {
        NewsData newsData = NewsData.builder()
                .id("LastSentenceTest#")
                .extID("extID#")
                .link("http://LastSentenceTest.somelink")
                .title(input.getLeft())
                .description(Optional.of(input.getRight()))
                .build();
        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentenceTest1: Last sentence result: {}", result);
        Assert.isTrue(!result.getDescription().isEmpty(), "New description is empty");
        Assert.isTrue(!result.getDescription().get().equals(newsData.getDescription().get()), "New description is equal old one");
        assertFalse(result.isRewritten(), "Rewritten flag is set incorrectly");
        //Assert.isTrue(newsData.getDescription().get().startsWith(result.getDescription().get()), "New description is not a part of old one");
    }

    // <String, String> - <title, description>
    static Stream<Pair<String, String>> tailCutProvider() {
        return Stream.of(
                Pair.of("Anduril is working on the difficult AI-related task of real-time edge computing.",
                        "Anduril announced its ninth acquisition on Monday with the purchase of Dublin’s Klas, makers of ruggedized edge computing equipment for the military and first-responders. Anduril wouldn’t reveal financial details of the deal, and the purchase is subject to regulatory approval, but the company did say that Klas employs 150 people. Relatedly, on Monday, Anduril also"),
                Pair.of("В Бат-Яме задержали мужчину с заряженным пистолетом.",
                        "Полиция сообщила, что в рамках борьбы с криминалом в Бат-Яме был задержан мужчина (21) с заряженным пистолетом калибра 9 мм. Оперативники из "),
                Pair.of("Палестинский поэт получил «Пулитцера» за эссе о Газе.",
                        "Поэт и основатель первой англоязычной библиотеки в Газе Мусаб Абу Тоха был удостоен Пулитцеровской премии за публицистические эссе. В них он ")
        );
    }


    @ParameterizedTest
    @MethodSource("tailRemoveProvider")
    void lastSentence_tailRemoval(Pair<String, String> input) {
        NewsData newsData = NewsData.builder()
                .id("lastSentenceTest3")
                .link("http://dddd.llll.zz")
                .title(input.getLeft())
                .description(Optional.of(input.getRight()))
                .build();

        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentence_tailRemoval: Last sentence result: {}", result);
        assertTrue(result.getDescription().isEmpty(), "Meaningless last sentence was not removed");
        assertFalse(result.isRewritten(), "Rewritten flag is set, while content was not changed");
    }


    // <String, String> - <title, description>
    static Stream<Pair<String, String>> tailRemoveProvider() {
        return Stream.of(
                Pair.of("Спецпосланник Бёлер: Боевые действия в Газе прекратятся с возвращением всех заложников.",
                        "Адам Бёлер, недавно назначенный президентом США Дональдом Трампом специальным посланником по реагированию на захват заложников, заявил"),
                Pair.of("В Бат-Яме задержали мужчину с заряженным пистолетом.",
                        "Оперативники из ")
        );
    }


    @Test
    void lastSentenceTest4() {
        NewsData newsData = NewsData.builder()
                .id("lastSentenceTest4")
                .link("http://dddd.llll.zz")
                .title("В Бат-Яме задержали мужчину с заряженным пистолетом.")
                .description(Optional.of("Полиция сообщила, что в рамках борьбы с криминалом в Бат-Яме был задержан мужчина (21) с заряженным пистолетом калибра 9 мм"))
                .build();

        NewsData result = lastSentenceProcessor.process(newsData);
        logger.info("lastSentenceTest4: Last sentence result: {}", result);
        assertFalse(result.getDescription().isEmpty(), "New description is empty");
        assertEquals(result.getDescription().get(), newsData.getDescription().get(), "New description is not equal old one");
        assertFalse(result.isRewritten(), "Rewritten flag is set, while content was not changed");
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
        assertTrue(!result.getDescription().isEmpty(), "New description is empty");
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
        assertTrue(!result.getDescription().isEmpty(), "New description is empty");
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
        assertTrue(!result.getDescription().isEmpty(), "New description is empty");
    }

}
