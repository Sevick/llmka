package integration;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.LLMProvider.impl.LLMProviderLocalOllama;
import com.fbytes.llmka.service.LLMService.LLMService;
import com.fbytes.llmka.service.NewsCheck.INewsCheck;
import com.fbytes.llmka.service.NewsCheck.impl.NewsCheckAd;
import config.TestConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integration")
@SpringBootTest(classes = {LLMProviderLocalOllama.class, LLMService.class, NewsCheckAd.class})
@ContextConfiguration(classes = {TestConfig.class})
class NewsCheckAdIntegrationTest {

    @Autowired
    INewsCheck newsCheckAd;

    @ParameterizedTest
    @MethodSource("nonAdProvider")
    void testCheckAd_nonAd(Pair<String, String>  input) {
        NewsData newsData1 = NewsData.builder()
                .id("1")
                .link("http://1")
                .title(input.getLeft())
                .description(Optional.of(input.getRight()))
                .build();
        Optional<INewsCheck.RejectReason> result = newsCheckAd.checkNews("testschema", newsData1);
        Assert.isTrue(result.isEmpty(), "newsCheckAd service returned result with non-empty reject reason (false alarm). NewsData: " + input);
    }

    // <String, String> - <title, description>
    static Stream<Pair<String, String>> nonAdProvider() {
        return Stream.of(
                Pair.of("China To Restrict US Film Releases.",
                        "Hours after Donald Trump imposed record 125% tariffs on Chinese products entering the US, China has announced it will further curb the number of US films allowed to screen in the country. From a report: \"The wrong action of the US government to abuse tariffs on China will inevitably further reduce the domestic audience's favourability towards American films,\" the China Film Administration said in a statement on Thursday. \"We will follow the market rules, respect the audience's choice, and moderately reduce the number of American films imported.\" The move mirrors the potential countermeasure suggested by two influential Chinese bloggers earlier in the week, warning that \"China has plenty of tools for retaliation.\" Both Liu Hong, a senior editor at Xinhuanet, the website of the state-run Xinhua news agency, as well as Ren Yi, the grandson of former Guangdong party chief Ren Zhongyi, posted an identical proposal involving a heavy reduction on the import of US movies and further investigation of the intellectual property benefits of American companies operating in China. China is the world's second largest film market after the US."),
                Pair.of("Trump replaces Obama portrait with painting of assassination attempt.",
                        "Trump replaced Obama's portrait with one of himself surviving an assassination attempt in Pennsylvania, while Obama's portrait was relocated."),
                Pair.of("Fed up with unruly Israeli tourists, Thailand develops an AI bot to teach them how to behave.",
                        "Israelis are visiting the East Asian country in record numbers, but after a few violent incidents, the Thai authorities are keen to give a lesson in etiquette.")
        );
    }


    @ParameterizedTest
    @MethodSource("adProvider")
    void testCheckAd_ad(Pair<String, String>  input) {
        NewsData newsData1 = NewsData.builder()
                .id("1")
                .link("http://1")
                .title(input.getLeft())
                .description(Optional.of(input.getRight()))
                .build();
        Optional<INewsCheck.RejectReason> result = newsCheckAd.checkNews("testschema", newsData1);
        Assert.isTrue(!result.isEmpty(), "newsCheckAd passed ad (missed alarm). NewsData: " + input);
    }

    // <String, String> - <title, description>
    static Stream<Pair<String, String>> adProvider() {
        return Stream.of(
                Pair.of("Labor Day Sale: How to Get Legitimate Office Suites for Merely $17.04? Microsoft Lifetime Licenses Enjoy 90% Discount! Let’s Get It! ",
                        "You’ve been up late grinding away, finally finishing up your work so you can enjoy your holiday, then, out of nowhere, Word and PowerPoint freeze ... The post appeared first on .")
        );
    }

}
