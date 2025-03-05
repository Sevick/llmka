import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.datasource.RssDataSource;
import com.fbytes.llmka.service.DataRetriver.IDataRetriever;
import com.fbytes.llmka.service.DataRetriver.impl.RssRetriever;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = RssRetriever.class)
public class RssRetriverTest {

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private IDataRetriever<RssDataSource> rssRetriever;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    public void testRss() {
        String rssUrl = "https://someurl/rss/news.xml";

        Resource resource = resourceLoader.getResource("classpath:rusnews.rss");
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(resource.getURI())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        when(restTemplate.getForObject(rssUrl, byte[].class)).thenReturn(content.getBytes(StandardCharsets.UTF_8));

        Optional<Stream<NewsData>> result = rssRetriever.retrieveData(new RssDataSource("DatasourceID", "RssRetriver", rssUrl));
        List<NewsData> resultList = result.orElseThrow().toList();
        verify(restTemplate, times(1)).getForObject(rssUrl, byte[].class);
        verifyNoMoreInteractions(restTemplate);
        Assert.isTrue(resultList.size() == 15, "Error parsing paragon RSS data");
    }


    @Test
    public void testRssEncoding() {
        String rssUrl = "https://someurl/rss/utf8news.xml";

        Resource resource = resourceLoader.getResource("classpath:rusUTF.rss");
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(resource.getURI())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        when(restTemplate.getForObject(rssUrl, byte[].class)).thenReturn(content.getBytes(StandardCharsets.UTF_8));

        Optional<Stream<NewsData>> result = rssRetriever.retrieveData(new RssDataSource("DatasourceID", "RssRetriver", rssUrl));
        List<NewsData> resultList = result.orElseThrow().toList();
        Assert.isTrue(resultList.size() == 1, "Error parsing paragon RSS data");
        Assert.isTrue(resultList.get(0).getTitle().equals("Текст."), "UTF8 text corrupted: Russian");
        Assert.isTrue(resultList.get(0).getDescription().orElse("").equals("תאור"), "UTF8 text corrupted: Hebrew");
    }
}
