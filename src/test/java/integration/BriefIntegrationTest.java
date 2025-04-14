package integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.BriefMaker.impl.BriefMaker;
import com.fbytes.llmka.service.LLMProvider.impl.LLMProviderLocalOllama;
import com.fbytes.llmka.service.LLMService.impl.LLMService;
import config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import java.util.Optional;

@ActiveProfiles("integration")
@SpringBootTest(classes = {LLMProviderLocalOllama.class, LLMService.class, BriefMaker.class})
@ContextConfiguration(classes = {TestConfig.class})
class BriefIntegrationTest {
    private static final Logger logger = Logger.getLogger(BriefIntegrationTest.class);

    @Autowired
    BriefMaker briefMaker;

    @Test
    void testBriefEng1() {
        String longText = "Hours after Donald Trump imposed record 125% tariffs on Chinese products entering the US, China has announced it will further curb the number of US films allowed to screen in the country. From a report: \"The wrong action of the US government to abuse tariffs on China will inevitably further reduce the domestic audience's favourability towards American films,\" the China Film Administration said in a statement on Thursday. \"We will follow the market rules, respect the audience's choice, and moderately reduce the number of American films imported.\" The move mirrors the potential countermeasure suggested by two influential Chinese bloggers earlier in the week, warning that \"China has plenty of tools for retaliation.\" Both Liu Hong, a senior editor at Xinhuanet, the website of the state-run Xinhua news agency, as well as Ren Yi, the grandson of former Guangdong party chief Ren Zhongyi, posted an identical proposal involving a heavy reduction on the import of US movies and further investigation of the intellectual property benefits of American companies operating in China. China is the world's second largest film market after the US.";
        NewsData newsData = NewsData.builder()
                .id("1")
                .link("http://1")
                .title("China To Restrict US Film Releases.")
                .description(Optional.of(longText))
                .build();

        NewsData result = briefMaker.makeBrief(newsData);
        logResults(newsData, result);
        Assert.isTrue(!result.getDescription().isEmpty(), "Brief service returned result with empty description");
        Assert.isTrue(result.getDescription().get().length() < newsData.getDescription().get().length(), "Brief service returned result with description not shorter than original");
    }

    @Test
    void testBriefEng2() {
        String longText = "Typically, complexity in programming is managed by breaking down tasks into subtasks. These subtasks can then be executed concurrently.Since Java 5, ExecutorService API helps the programmer execute these subtasks concurrently. However, given the nature of concurrent execution, each subtask could fail or succeed independently with no implicit communication between them. The failure of one subtask does not automatically cancel the other subtasks. Although an attempt can be made to manage this cancellation manually via external handling, it's quite tricky to get it right — especially when a large number of subtasks are involved.";
        NewsData newsData = NewsData.builder()
                .id("2")
                .link("https://dzone.com/articles/understanding-structured-concurrency-java")
                .title("Understanding Structured Concurrency in Java.")
                .description(Optional.of(longText))
                .build();

        NewsData result = briefMaker.makeBrief(newsData);
        logResults(newsData, result);
        Assert.isTrue(!result.getDescription().isEmpty(), "Brief service returned result with empty description");
        Assert.isTrue(result.getDescription().get().length() < newsData.getDescription().get().length(), "Brief service returned result with description not shorter than original");
    }

    @Test
    void testBriefRus() {
        String longText = "Как сообщило Федеральное управление гражданской авиации США, в четверг в Национальном аэропорту имени Рейгана в Вашингтоне столкнулись два самолета American Airlines. На борту одного из них находились Кит и Авива Сигаль - бывшие заложники, освобожденные из плена ХАМАСа.";
        NewsData newsData = NewsData.builder()
                .id("3")
                .link("http://1")
                .title("В аэропорту США столкнулись два самолета. На борту одного из них находились бывшие заложники ХАМАСа")
                .description(Optional.of(longText))
                .build();

        NewsData result = briefMaker.makeBrief(newsData);
        logResults(newsData, result);
        Assert.isTrue(result.getDescription().get().length() < newsData.getDescription().get().length(), "Brief service returned result with description not shorter than original");
    }

    private void logResults(NewsData src, NewsData result){
        logger.info("Brief transformation:\nFrom:\n  Title: {}\n  Descr: {}\nTo:\n  Title: {}\n  Descr: {}",
                src.getTitle(),src.getDescription().get(),
                result.getTitle(), result.getDescription().get());
    }
}
