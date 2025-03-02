package com.fbytes.llmka.service.DataRetriver.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.datasource.RssDataSource;
import com.fbytes.llmka.service.DataRetriver.DataRetriever;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;


@Service
public class RssRetriever extends DataRetriever<RssDataSource> {

    private static final Logger logger = Logger.getLogger(RssRetriever.class);

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Optional<Stream<NewsData>> retrieveData(RssDataSource dataSource) {
        try {
            SyndFeedInput input = new SyndFeedInput();
            String feedStr = restTemplate.getForObject(dataSource.getUrl(), String.class);
            InputStream inputStream = new ByteArrayInputStream(feedStr.getBytes(StandardCharsets.UTF_8));
            SyndFeed feed = input.build(new XmlReader(inputStream));
            List<SyndEntry> entryList = feed.getEntries();

            Stream<NewsData> result = entryList.stream()
                    .map(entry ->
                            NewsData.builder()
                                    .id(UUID.randomUUID().toString())
                                    .dataSourceID(dataSource.getId())
                                    .dataSourceName(dataSource.getName())
                                    .link(entry.getLink())
                                    .title(entry.getTitle())
                                    .description(Optional.ofNullable(entry.getDescription())
                                            .map(title -> Jsoup.parse(title.getValue()).text()))
                                    .text(Optional.ofNullable(entry.getContents())
                                            .filter(contents -> !contents.isEmpty())
                                            .map(contents -> Jsoup.parse(((SyndContent) contents.get(0)).getValue()))
                                            .map(document  -> {
                                                document.select("a").remove();
                                                return document.text();
                                            }))
                                    .build()
                    );
            logger.debug("Read {} bytes, entries processed: {}", feedStr.getBytes().length, entryList.size());
            return Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
