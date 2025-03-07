package real;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.datasource.RssDataSource;
import com.fbytes.llmka.service.DataRetriver.IDataRetriever;
import com.fbytes.llmka.service.DataRetriver.impl.RssRetriever;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootTest(classes = {RssRetriever.class, RestTemplate.class})
public class RssRetriverInegrationTest {

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private IDataRetriever<RssDataSource> rssRetriever;

    @Disabled
    @Test
    public void testRssEncoding() {
        String rssUrl = "https://www.vesty.co.il/3rdparty/mobile/rss/vesty/13148/";

        Optional<Stream<NewsData>> result = rssRetriever.retrieveData(new RssDataSource("DatasourceID", "RssRetriver", rssUrl));
        List<NewsData> resultList = result.orElseThrow().toList();
        Assert.isTrue(!resultList.isEmpty(), "Error parsing paragon RSS data");
    }
}
