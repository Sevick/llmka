package integration;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.LLMProvider.impl.LLMProviderLocalOllama;
import com.fbytes.llmka.service.LLMService.impl.LLMService;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import com.fbytes.llmka.service.NewsCheck.impl.NewsCheckAd;
import config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import java.util.Optional;

@ActiveProfiles("integration")
@SpringBootTest(classes = {LLMProviderLocalOllama.class, LLMService.class, NewsCheckAd.class})
@ContextConfiguration(classes = {TestConfig.class})
class NewsCheckAdIntegrationTest {

    @Autowired
    INewsCheck newsCheckAd;

    @Test
    void testCheckAdEng1() {
        String longText = "Hours after Donald Trump imposed record 125% tariffs on Chinese products entering the US, China has announced it will further curb the number of US films allowed to screen in the country. From a report: \"The wrong action of the US government to abuse tariffs on China will inevitably further reduce the domestic audience's favourability towards American films,\" the China Film Administration said in a statement on Thursday. \"We will follow the market rules, respect the audience's choice, and moderately reduce the number of American films imported.\" The move mirrors the potential countermeasure suggested by two influential Chinese bloggers earlier in the week, warning that \"China has plenty of tools for retaliation.\" Both Liu Hong, a senior editor at Xinhuanet, the website of the state-run Xinhua news agency, as well as Ren Yi, the grandson of former Guangdong party chief Ren Zhongyi, posted an identical proposal involving a heavy reduction on the import of US movies and further investigation of the intellectual property benefits of American companies operating in China. China is the world's second largest film market after the US.";
        NewsData newsData1 = NewsData.builder()
                .id("1")
                .link("http://1")
                .title("China To Restrict US Film Releases.")
                .description(Optional.of(longText))
                .build();

        Optional<INewsCheck.RejectReason> result = newsCheckAd.checkNews("testschema", newsData1);
        //Assert.isTrue(!result.getDescription().isEmpty(), "Brief service returned result with empty description");
        //Assert.isTrue(result.getDescription().get().length() < newsData1.getDescription().get().length(), "Brief service returned result with description not shorter than original");
    }

    @Test
    void testCheckAdEng2() {
        String longText = "Trump replaced Obama's portrait with one of himself surviving an assassination attempt in Pennsylvania, while Obama's portrait was relocated.";
        NewsData newsData1 = NewsData.builder()
                .id("1")
                .link("http://1")
                .title("Trump replaces Obama portrait with painting of assassination attempt.")
                .description(Optional.of(longText))
                .build();

        Optional<INewsCheck.RejectReason> result = newsCheckAd.checkNews("testschema", newsData1);
        Assert.isTrue(result.isEmpty(), "checkAd : false alarm");
    }
}
