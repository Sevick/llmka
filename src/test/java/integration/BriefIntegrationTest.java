package integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.DataRetriver.ParserRSS.IParserRSS;
import com.fbytes.llmka.service.DataRetriver.ParserRSS.ParserRSS;
import com.fbytes.llmka.service.LLMProvider.impl.LLMProviderLocalOllama;
import com.fbytes.llmka.service.LLMService.LLMService;
import com.fbytes.llmka.service.NewsProcessor.INewsProcessor;
import com.fbytes.llmka.service.NewsProcessor.impl.NewsProcessorBrief;
import config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Optional;

@EnabledIf(expression = "#{environment.acceptsProfiles('integration')}", reason = "Runs only for integration profile")
@SpringBootTest(classes = {LLMProviderLocalOllama.class, LLMService.class, NewsProcessorBrief.class, ParserRSS.class})
@ContextConfiguration(classes = {TestConfig.class})
class BriefIntegrationTest {
    private static final Logger logger = Logger.getLogger(BriefIntegrationTest.class);

    @Autowired
    ResourceLoader resourceLoader;
    @Autowired
    INewsProcessor briefMaker;
    @Autowired
    IParserRSS parserRSS;

    @Test
    void testBriefEng1() {
        String longText = "Hours after Donald Trump imposed record 125% tariffs on Chinese products entering the US, China has announced it will further curb the number of US films allowed to screen in the country. From a report: \"The wrong action of the US government to abuse tariffs on China will inevitably further reduce the domestic audience's favourability towards American films,\" the China Film Administration said in a statement on Thursday. \"We will follow the market rules, respect the audience's choice, and moderately reduce the number of American films imported.\" The move mirrors the potential countermeasure suggested by two influential Chinese bloggers earlier in the week, warning that \"China has plenty of tools for retaliation.\" Both Liu Hong, a senior editor at Xinhuanet, the website of the state-run Xinhua news agency, as well as Ren Yi, the grandson of former Guangdong party chief Ren Zhongyi, posted an identical proposal involving a heavy reduction on the import of US movies and further investigation of the intellectual property benefits of American companies operating in China. China is the world's second largest film market after the US.";
        NewsData newsData = NewsData.builder()
                .id("testBriefEng1")
                .link("http://1")
                .title("China To Restrict US Film Releases.")
                .description(Optional.of(longText))
                .build();

        NewsData result = briefMaker.process(newsData);
        logResults(newsData, result);
        Assert.isTrue(!result.getDescription().isEmpty(), "Brief service returned result with empty description");
        Assert.isTrue(result.getDescription().get().length() < newsData.getDescription().get().length(), "Brief service returned result with description not shorter than original");
    }

    @Test
    void testBriefEng2() {
        String longText = "Typically, complexity in programming is managed by breaking down tasks into subtasks. These subtasks can then be executed concurrently.Since Java 5, ExecutorService API helps the programmer execute these subtasks concurrently. However, given the nature of concurrent execution, each subtask could fail or succeed independently with no implicit communication between them. The failure of one subtask does not automatically cancel the other subtasks. Although an attempt can be made to manage this cancellation manually via external handling, it's quite tricky to get it right — especially when a large number of subtasks are involved.";
        NewsData newsData = NewsData.builder()
                .id("testBriefEng2")
                .link("https://dzone.com/articles/understanding-structured-concurrency-java")
                .title("Understanding Structured Concurrency in Java.")
                .description(Optional.of(longText))
                .build();

        NewsData result = briefMaker.process(newsData);
        logResults(newsData, result);
        Assert.isTrue(!result.getDescription().isEmpty(), "Brief service returned result with empty description");
        Assert.isTrue(result.getDescription().get().length() < newsData.getDescription().get().length(), "Brief service returned result with description not shorter than original");
    }

    @Test
    void testBriefRus1() {
        String longText = "Как сообщило Федеральное управление гражданской авиации США, в четверг в Национальном аэропорту имени Рейгана в Вашингтоне столкнулись два самолета American Airlines. На борту одного из них находились Кит и Авива Сигаль - бывшие заложники, освобожденные из плена ХАМАСа.";
        NewsData newsData = NewsData.builder()
                .id("testBriefRus1")
                .link("http://1")
                .title("В аэропорту США столкнулись два самолета. На борту одного из них находились бывшие заложники ХАМАСа")
                .description(Optional.of(longText))
                .build();

        NewsData result = briefMaker.process(newsData);
        logResults(newsData, result);
        Assert.isTrue(result.getDescription().get().length() > 10, MessageFormat.format("Brief service returns empty/wrong description. Description returned: {0}", result.getDescription().get()));
        Assert.isTrue(result.getDescription().get().length() <= newsData.getDescription().get().length(), "Brief service returned result with description not shorter than original");
    }

    @Test
    void testNoBrief() {
        String longText = "Some short description.";
        NewsData newsData = NewsData.builder()
                .id("testNoBrief")
                .link("http://1")
                .title("Some title")
                .description(Optional.of(longText))
                .build();

        NewsData result = briefMaker.process(newsData);
        logResults(newsData, result);
        Assert.isTrue(result.getDescription().get().length() == newsData.getDescription().get().length(), "Brief rewritten a short description");
    }

    @Test
    void testBriefRus2() {
        String longText = "Основной доход — около 19,2 миллиарда — обеспечили поставки боеприпасов и снаряжения. Ещё 630 миллионов долларов составили доходы от передачи технологий, а 280 миллионов — от отправки северокорейских военнослужащих в Россию.";
        NewsData newsData = NewsData.builder()
                .id("testBriefRus2")
                .link("http://1")
                .title("Северная Корея заработала на войне России с Украиной более 20 миллиардов долларов")
                .description(Optional.of(longText))
                .build();

        NewsData result = briefMaker.process(newsData);
        logResults(newsData, result);
        Assert.isTrue(result.getDescription().get().length() <= newsData.getDescription().get().length(), "Brief is not any shorter");
    }

    @Test
    void testBriefRus3() {
        NewsData newsData = NewsData.builder()
                .id("testBriefRus3")
                .link("http://11231")
                .title("В Секторе Газа сообщают о 23 погибших")
                .description(Optional.of("Спасательные службы в Секторе Газа сообщили, что вчера вечером в результате атак Армии обороны Израиля погибли 23 человека, большинство из них — женщины и дети. Десять человек погибли в результате атаки на палатки перемещенных лиц в районе Аль-Маваси в Хан-Юнисе, семь человек погибли в Джебалии и шесть человек погибли в Бейт-Лахии."))
                .build();

        NewsData result = briefMaker.process(newsData);
        logResults(newsData, result);
        Assert.isTrue(result.getDescription().get().length() <= newsData.getDescription().get().length(), "Brief rewritten a short description");
    }

    @Test
    void testBriefRSS() throws IOException, ParserRSS.ParsingException {

        String content = fetchTestResourceAsString("classpath:test-data/brief/brief.rss");
        NewsData[] newsData = parserRSS.parseRSS(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

        NewsData result = briefMaker.process(newsData[0]);
        logResults(newsData[0], result);
        Assert.isTrue(result.getDescription().get().length() > 10, MessageFormat.format("Brief service returns empty/wrong description. Description returned: {0}", result.getDescription().get()));
        Assert.isTrue(result.getDescription().get().length() < newsData[0].getDescription().get().length(), "Brief service returned result with description not shorter than original");
    }


    @Test
    void testBriefRus4() {
        NewsData newsData = NewsData.builder()
                .id("testBriefRus4")
                .link("http://dddd.llll.zz")
                .title("Умер раввин Мазуз, считавший репатриантов \"безбожниками\"")
                .description(Optional.of("Раввин Меир Мазуз скончался в возрасте 80 лет. Он считался одним из самых известных толкователей Галахи у сефардских евреев, в прошлом входил в Совет мудрецов Торы (высший орган партии ШАС), но потом покинул его, публично поддержав Эли Ишая. Впоследствии Мазуз вернулся в ШАС. Он регулярно обрушивался с нападками на русскоязычных репатриантов, считая их \"безбожниками\"."))
                .build();

        NewsData result = briefMaker.process(newsData);
        logger.info("lastSentenceTest5: Last sentence result: {}", result);
        Assert.isTrue(!result.getDescription().isEmpty(), "New description is empty");
    }


    private void logResults(NewsData src, NewsData result) {
        logger.info("Brief transformation:\nFrom:\n  Title: {}\n  Descr: {}\nTo:\n  Title: {}\n  Descr: {}",
                src.getTitle(), src.getDescription().get(),
                result.getTitle(), result.getDescription().get());
    }

    private String fetchTestResourceAsString(String resourcePath) throws IOException {
        Resource resource = resourceLoader.getResource(resourcePath);
        return new String(Files.readAllBytes(Paths.get(resource.getURI())), StandardCharsets.UTF_8);
    }
}
