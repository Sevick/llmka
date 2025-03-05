package com.fbytes.llmka.service.DataRetriver.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.datasource.RssDataSource;
import com.fbytes.llmka.service.DataRetriver.DataRetriever;
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
import java.util.Arrays;
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
            byte[] feedStr = restTemplate.getForObject(dataSource.getUrl(), byte[].class);
            if (feedStr==null)
                return Optional.empty();
            InputStream inputStream = new ByteArrayInputStream(feedStr);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(inputStream));
            List<SyndEntry> entryList = feed.getEntries();

            NewsData[] result = entryList.stream()
                    .map(entry -> {
                                String titleStr = entry.getTitle().transform(str -> {
                                    if (str.charAt(str.length() - 1) != '.')
                                        return str + ".";
                                    else
                                        return str;
                                });
                                Optional<String> descrSrc = Optional.ofNullable(entry.getDescription().getValue());
                                Optional<String> contentSrc = Optional.empty();

                                if (descrSrc.isEmpty()) {
                                    descrSrc = contentSrc;
                                    contentSrc = Optional.empty();
                                }

                                Optional<String> descr = descrSrc
                                        .map(Jsoup::parse)
                                        .map(document -> {
                                            document.select("a").remove();
                                            return document.text();
                                        });
                                Optional<String> content = contentSrc
                                        .map(Jsoup::parse)
                                        .map(document -> {
                                            document.select("a").remove();
                                            return document.text();
                                        });

                                return NewsData.builder()
                                        .id(UUID.randomUUID().toString())
                                        .dataSourceID(dataSource.getId())
                                        .dataSourceName(dataSource.getName())
                                        .link(entry.getLink())
                                        .title(titleStr)
                                        .description(descr)
                                        .text(content)
                                        .build();
                            }
                    ).toArray(size -> new NewsData[size]);
            logger.debug("Read {} bytes, entries processed: {}", feedStr.length, result.length);
            return Optional.of(Arrays.stream((NewsData[]) result));
        } catch (Exception e) {
            logger.error("DataSource: {}, exception: {}", dataSource.getName(), e.getMessage());
            return Optional.empty();
        }
    }
}
