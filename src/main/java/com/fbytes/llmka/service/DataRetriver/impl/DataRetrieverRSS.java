package com.fbytes.llmka.service.DataRetriver.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.config.newssource.RssNewsSource;
import com.fbytes.llmka.service.DataRetriver.DataRetriever;
import com.fbytes.llmka.service.DataRetriver.ParserRSS.IParserRSS;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class DataRetrieverRSS extends DataRetriever<RssNewsSource> {

    private static final Logger logger = Logger.getLogger(DataRetrieverRSS.class);

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private IParserRSS parserRSS;

    private final HttpEntity<String> httpEntity;

    public DataRetrieverRSS() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/rss+xml");
        headers.add("User-Agent", "Postman");
        this.httpEntity = new HttpEntity<>(headers);
    }

    @Override
    @Timed(value = "llmka.dataretrive.rss.time", description = "time retrieve RSS data", percentiles = {0.5, 0.9})
    public Optional<Stream<NewsData>> retrieveData(RssNewsSource dataSource) {
        try {
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(dataSource.getUrl(), HttpMethod.GET, httpEntity, byte[].class);
            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null || responseEntity.getBody().length == 0) {
                logger.debug("Failed to retrieve data from: {}\nResponseStatus: {}, ResponseBody: {}",
                        dataSource.getUrl(), responseEntity.getStatusCode(), responseEntity.getBody());
                return Optional.empty();
            }
            byte[] feedStr = responseEntity.getBody();
            if (feedStr == null || feedStr.length==0){
                logger.debug("Empty response body from: {}", dataSource.getUrl());
                return Optional.empty();
            }
            logger.debug("read {} bytes", feedStr.length);
            NewsData[] result = parserRSS.parseRSS(new ByteArrayInputStream(feedStr));
            logger.debug("entries processed: {}", result.length);
            return Optional.of(Arrays.stream(result)
                    .map(newsData -> {
                        newsData.setDataSourceID(dataSource.getId());
                        newsData.setDataSourceName(dataSource.getName());
                        return newsData;
                    }));
        } catch (Exception e) {
            logger.logException(e);
            return Optional.empty();
        }
    }
}
