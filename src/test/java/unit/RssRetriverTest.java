package unit;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.config.newssource.RssNewsSource;
import com.fbytes.llmka.service.DataRetriver.IDataRetriever;
import com.fbytes.llmka.service.DataRetriver.ParserRSS.ParserRSS;
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

@SpringBootTest(classes = {DataRetrieverRSS.class, ParserRSS.class})
class RssRetriverTest {
    private static final Logger logger = Logger.getLogger(RssRetriverTest.class);
    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private IDataRetriever<RssNewsSource> rssRetriever;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void testRss() throws IOException {
        String rssUrl = "https://someurl/rss/news.xml";
        String content = fetchTestResourceAsString("classpath:test-data/parse-rss/rusnews.rss");
        HttpEntity httpEntity = fetchHttpEntity();
        when(restTemplate.exchange(rssUrl, HttpMethod.GET, httpEntity, byte[].class)).thenReturn(ResponseEntity.ok(content.getBytes(StandardCharsets.UTF_8)));

        Optional<Stream<NewsData>> result = rssRetriever.retrieveData(new RssNewsSource("DatasourceID", "RssRetriver", rssUrl, "GroupName"));
        List<NewsData> resultList = result.orElseThrow().toList();
        verify(restTemplate, times(1)).exchange(rssUrl, HttpMethod.GET, httpEntity, byte[].class);
        verifyNoMoreInteractions(restTemplate);
        Assert.isTrue(resultList.size() == 9, "Error parsing paragon RSS data. Expected 9 items, but got " + resultList.size());
    }


    @Test
    void testRss404() throws IOException {
        String rssUrl = "https://someurl/rss/utf8news.xml";
        when(restTemplate.exchange(rssUrl, HttpMethod.GET, fetchHttpEntity(), byte[].class)).thenReturn(ResponseEntity.notFound().build());

        Optional<Stream<NewsData>> result = rssRetriever.retrieveData(new RssNewsSource("DatasourceID", "RssRetriver", rssUrl, "GroupName"));

        Assert.isTrue(result != null, "Wrong return - should be empty optional (instead of null)");
        Assert.isTrue(result.isEmpty(), "Wrong return - should be empty optional");
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
