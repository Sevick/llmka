import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.newssource.RssNewsSource;
import com.fbytes.llmka.service.DataRetriver.IDataRetriever;
import com.fbytes.llmka.service.DataRetriver.impl.DataRetrieverRSS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = DataRetrieverRSS.class)
public class RssRetriverTest {

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private IDataRetriever<RssNewsSource> rssRetriever;

    @MockitoBean
    private RestTemplate restTemplate;


    @Test
    public void testRss() throws IOException {
        String rssUrl = "https://someurl/rss/news.xml";
        String content = fetchTestResourceAsString("classpath:test-data/rusnews.rss");
        HttpEntity httpEntity = fetchHttpEntity();
        when(restTemplate.exchange(rssUrl, HttpMethod.GET, httpEntity, byte[].class)).thenReturn(ResponseEntity.ok(content.getBytes(StandardCharsets.UTF_8)));

        Optional<Stream<NewsData>> result = rssRetriever.retrieveData(new RssNewsSource("DatasourceID", "RssRetriver", rssUrl, "GroupName"));
        List<NewsData> resultList = result.orElseThrow().toList();
        verify(restTemplate, times(1)).exchange(rssUrl, HttpMethod.GET, httpEntity, byte[].class);
        verifyNoMoreInteractions(restTemplate);
        Assert.isTrue(resultList.size() == 15, "Error parsing paragon RSS data");
    }


    @Test
    public void testRss1() throws IOException {
        String rssUrl = "https://someurl/rss/news.xml";
        String content = fetchTestResourceAsString("classpath:test-data/israelinfo.rss");
        HttpEntity httpEntity = fetchHttpEntity();
        when(restTemplate.exchange(rssUrl, HttpMethod.GET, httpEntity, byte[].class)).thenReturn(ResponseEntity.ok(content.getBytes(StandardCharsets.UTF_8)));

        Optional<Stream<NewsData>> result = rssRetriever.retrieveData(new RssNewsSource("DatasourceID", "RssRetriver", rssUrl, "GroupName"));
        List<NewsData> resultList = result.orElseThrow().toList();
        verify(restTemplate, times(1)).exchange(rssUrl, HttpMethod.GET, httpEntity, byte[].class);
        verifyNoMoreInteractions(restTemplate);
        Assert.isTrue(resultList.size() == 100, "Error parsing paragon RSS data");
        resultList.forEach(res -> {
            Assert.isTrue(!checkForTags(res.getTitle()), "tags detected in NewsData");
            Assert.isTrue(!checkForTags(res.getDescription().orElse("")), "tags detected in NewsData");
        });
    }


    @Test
    public void testRssEncoding() throws IOException {
        String rssUrl = "https://someurl/rss/utf8news.xml";
        String content = fetchTestResourceAsString("classpath:test-data/rusUTF.rss");
        when(restTemplate.exchange(rssUrl, HttpMethod.GET, fetchHttpEntity(), byte[].class)).thenReturn(ResponseEntity.ok(content.getBytes(StandardCharsets.UTF_8)));

        Optional<Stream<NewsData>> result = rssRetriever.retrieveData(new RssNewsSource("DatasourceID", "RssRetriver", rssUrl, "GroupName"));
        List<NewsData> resultList = result.orElseThrow().toList();
        Assert.isTrue(resultList.size() == 1, "Error parsing paragon RSS data");
        Assert.isTrue(resultList.get(0).getTitle().equals("Текст."), "UTF8 text corrupted: Russian");
        Assert.isTrue(resultList.get(0).getDescription().orElse("").equals("תאור."), "UTF8 text corrupted: Hebrew");
        resultList.forEach(res -> {
            Assert.isTrue(!checkForTags(res.getTitle()), "tags detected in NewsData");
            Assert.isTrue(!checkForTags(res.getDescription().orElse("")), "tags detected in NewsData");
        });
    }


    @Test
    public void testRssFullText() throws IOException {
        String rssUrl = "https://someurl/rss/utf8news.xml";
        String content = fetchTestResourceAsString("classpath:test-data/fulltext.rss");
        when(restTemplate.exchange(rssUrl, HttpMethod.GET, fetchHttpEntity(), byte[].class)).thenReturn(ResponseEntity.ok(content.getBytes(StandardCharsets.UTF_8)));

        Optional<Stream<NewsData>> result = rssRetriever.retrieveData(new RssNewsSource("DatasourceID", "RssRetriver", rssUrl, "GroupName"));
        List<NewsData> resultList = result.orElseThrow().toList();
        Assert.isTrue(resultList.size() == 1, "Error parsing paragon RSS data");
        resultList.forEach(res -> {
            Assert.isTrue(!checkForTags(res.getTitle()), "tags detected in NewsData");
            Assert.isTrue(!checkForTags(res.getDescription().orElse("")), "tags detected in NewsData");
        });
    }


    @Test
    public void testHtmlTagsFinder() {
        String textWithTags1 = "<title><![CDATA[aasdbcbcvb]]></title>";
        String textWithTags2 = "some text <a img=> and more text";
        String textNoTags = "a ref link a";
        Assert.isTrue(checkForTags(textWithTags1), "Tags were not detected correctly");
        Assert.isTrue(checkForTags(textWithTags2), "Tags were not detected correctly");
        Assert.isTrue(!checkForTags(textNoTags), "False tags detection on text with no tags");
    }


    @Test
    public void testRss404() throws IOException {
        String rssUrl = "https://someurl/rss/utf8news.xml";
        when(restTemplate.exchange(rssUrl, HttpMethod.GET, fetchHttpEntity(), byte[].class)).thenReturn(ResponseEntity.notFound().build());

        Optional<Stream<NewsData>> result = rssRetriever.retrieveData(new RssNewsSource("DatasourceID", "RssRetriver", rssUrl, "GroupName"));

        Assert.isTrue(result != null, "Wrong return - should be empty optional (instead of null)");
        Assert.isTrue(result.isEmpty(), "Wrong return - should be empty optional");
    }


    @Test
    public void testRssNoGuid() throws IOException {
        String rssUrl = "https://someurl/rss/utf8news.xml";
        String content = fetchTestResourceAsString("classpath:test-data/rss-xml/noguid.rss");
        when(restTemplate.exchange(rssUrl, HttpMethod.GET, fetchHttpEntity(), byte[].class)).thenReturn(ResponseEntity.ok(content.getBytes(StandardCharsets.UTF_8)));

        Optional<Stream<NewsData>> result = rssRetriever.retrieveData(new RssNewsSource("DatasourceID", "RssRetriver", rssUrl, "GroupName"));
        List<NewsData> resultList = result.orElseThrow().toList();

        Assert.isTrue(resultList.size() == 1, "Error parsing paragon RSS data");
        Assert.isTrue(!resultList.get(0).getId().isEmpty(), "Empty GUID leads to empty NewsData.id");
    }


    private HttpEntity<String> fetchHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/rss+xml");
        headers.add("User-Agent", "Postman");
        return new HttpEntity<String>(headers);
    }

    private boolean checkForTags(String src) {
        String regex = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        return matcher.find();
    }

    private String fetchTestResourceAsString(String resourcePath) throws IOException {
        Resource resource = resourceLoader.getResource(resourcePath);
        return new String(Files.readAllBytes(Paths.get(resource.getURI())), StandardCharsets.UTF_8);
    }
}
