package unit;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.config.newssource.NewsSource;
import com.fbytes.llmka.model.config.newssource.RssNewsSource;
import com.fbytes.llmka.service.DataRetriver.ParserRSS.IParserRSS;
import com.fbytes.llmka.service.DataRetriver.ParserRSS.ParserRSS;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {ParserRSS.class})
class RssParserTest {
    private static final Logger logger = Logger.getLogger(RssParserTest.class);

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private IParserRSS parserRSS;

    @ParameterizedTest
    @MethodSource("resourcePathProvider")
    void testRss(Pair<String, Integer> input) throws IOException, ParserRSS.ParsingException {
        logger.debug("Testing RSS parsing for resource: {}", input.getLeft());
        byte[] content = fetchTestResource(input.getLeft());
        NewsData[] newsDataArr = parserRSS.parseRSS(new ByteArrayInputStream(content));
        Assert.isTrue(newsDataArr.length == input.getRight(), "Error parsing paragon RSS data");
        for (NewsData newsData : newsDataArr) {
            logger.debug("Verifying parsed entries: {}", newsData);
            Assert.isTrue(newsData.getId() != null, "ID is null");
            Assert.isTrue(!newsData.getId().isEmpty(), "ID is empty");
            Assert.isTrue(newsData.getTitle() != null, "Title is null");
            Assert.isTrue(!newsData.getTitle().isEmpty(), "Title is empty");
            Assert.isTrue(newsData.getDescription().isPresent(), "Description is not present");
            Assert.isTrue(!newsData.getDescription().isEmpty(), "Description is empty");
            Assert.isTrue(!checkForTags(newsData.getTitle()), "Title contains HTML tags");
            Assert.isTrue(!checkForTags(newsData.getDescription().get()), "Description contains HTML tags");
        }
    }

    // <String, Integer> - <resourcePath, expectedCount>
    static Stream<Pair<String, Integer>> resourcePathProvider(){
        return Stream.of(
                Pair.of("classpath:test-data/parse-rss/fulltext.rss", 1),
                Pair.of("classpath:test-data/parse-rss/rusUTF.rss",1),
                Pair.of("classpath:test-data/parse-rss/noguid.rss", 1),
                Pair.of("classpath:test-data/parse-rss/rusnews.rss", 9)
        );
    }

    @Test
    void testTechCrunch() throws IOException, ParserRSS.ParsingException {
        byte[] content = fetchTestResource("classpath:test-data/parse-rss/techcrunch.rss");
        NewsData[] newsDataArr = parserRSS.parseRSS(new ByteArrayInputStream(content));
        assertEquals(newsDataArr.length, 2);
        assertEquals(newsDataArr[0].getTitle(), "Anduril is working on the difficult AI-related task of real-time edge computing.");
        assertEquals(newsDataArr[0].getLink(), "https://techcrunch.com/2025/05/05/anduril-is-working-on-the-difficult-ai-related-task-of-real-time-edge-computing/");
        assertEquals(newsDataArr[0].getDescription().get(), "Anduril announced its ninth acquisition on Monday with the purchase of Dublin’s Klas, makers of ruggedized edge computing equipment for the military and first-responders. Anduril wouldn’t reveal financial details of the deal, and the purchase is subject to regulatory approval, but the company did say that Klas employs 150 people. Relatedly, on Monday, Anduril also");
    }

    @Test
    void testGizChina() throws IOException, ParserRSS.ParsingException {
        byte[] content = fetchTestResource("classpath:test-data/parse-rss/gizchina.rss");
        NewsData[] newsDataArr = parserRSS.parseRSS(new ByteArrayInputStream(content));
        assertEquals(newsDataArr[0].getDescription().get(), "Honor’s 400 series launch is imminent. Following the previous appearance of Honor 400 Pro on Geekbench, now even the vanilla Honor 400 has shown up");
    }

    @Test
    void testHtmlTagsFinder() {
        String textWithTags1 = "<title><![CDATA[aasdbcbcvb]]></title>";
        String textWithTags2 = "some text <a img=> and more text";
        String textNoTags = "a ref link a";
        Assert.isTrue(checkForTags(textWithTags1), "Tags were not detected correctly");
        Assert.isTrue(checkForTags(textWithTags2), "Tags were not detected correctly");
        Assert.isTrue(!checkForTags(textNoTags), "False tags detection on text with no tags");
    }


    private boolean checkForTags(String src) {
        String regex = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        return matcher.find();
    }

    private byte[] fetchTestResource(String resourcePath) throws IOException {
        Resource resource = resourceLoader.getResource(resourcePath);
        return Files.readAllBytes(Paths.get(resource.getURI()));
    }
}
