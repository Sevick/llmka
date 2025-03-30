package com.fbytes.llmka.service.DataRetriver.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.newssource.RssNewsSource;
import com.fbytes.llmka.service.DataRetriver.DataRetriever;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class RssRetriever extends DataRetriever<RssNewsSource> {

    private static final Logger logger = Logger.getLogger(RssRetriever.class);

    @Autowired
    private RestTemplate restTemplate;

    private final HttpEntity<String> httpEntity;

    public RssRetriever() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/rss+xml");
        headers.add("User-Agent", "Postman");
        this.httpEntity = new HttpEntity<>(headers);
    }

    @Override
    public Optional<Stream<NewsData>> retrieveData(RssNewsSource dataSource) {
        try {
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(dataSource.getUrl(), HttpMethod.GET, httpEntity, byte[].class);
            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null || responseEntity.getBody().length == 0)
                return Optional.empty();
            byte[] feedStr = responseEntity.getBody();
            NewsData[] result = parseRSS(new ByteArrayInputStream(feedStr), dataSource);
            logger.debug("Read {} bytes, entries processed: {}", feedStr.length, result.length);
            return Optional.of(Arrays.stream((NewsData[]) result));
        } catch (Exception e) {
            logger.error("DataSource: {}, exception: {}", dataSource.getName(), e.getMessage());
            return Optional.empty();
        }
    }


    private NewsData[] parseRSS(InputStream inputStream, RssNewsSource dataSource) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        doc.getDocumentElement().normalize();

        NodeList itemList = doc.getElementsByTagName("item");
        List<NewsData> result = new ArrayList<>();
        for (int i = 0; i < itemList.getLength(); i++) {
            Node itemNode = itemList.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) itemNode;
                Optional<String> guid = Optional.ofNullable(itemElement.getElementsByTagName("guid").item(0))
                        .map(item -> item.getTextContent().toString());
                String title = itemElement.getElementsByTagName("title").item(0).getTextContent().toString()
                        .transform(txt -> checkAddLastDot(txt));
                String link = itemElement.getElementsByTagName("link").item(0).getTextContent().toString();
                Optional<String> description = Optional.ofNullable(itemElement.getElementsByTagName("description").item(0))
                        .map(el -> parseElement(el));
                Optional<String> fullText = Optional.ofNullable(itemElement.getElementsByTagName("full-text").item(0))
                        .map(el -> parseElement(el));
                if (description.isEmpty() || description.get().isEmpty()) {
                    description = getFirstSentense(fullText);
                }
                title = title.transform(txt -> checkAddLastDot(txt));
                description = description.map(txt -> checkAddLastDot(txt));
                String extId = guid.orElse(link);

                result.add(
                        NewsData.builder()
                                .id(extId)
                                .dataSourceID(dataSource.getId())
                                .dataSourceName(dataSource.getName())
                                .link(link)
                                .title(title)
                                .description(description)
                                .text(Optional.empty())
                                .build()
                );
            }
        }
        return result.toArray(NewsData[]::new);
    }

    private String parseElement(Node src) {
        org.jsoup.nodes.Document doc = Jsoup.parse(src.getTextContent().replaceAll("<!\\[CDATA\\[(.*)\\]\\]>", "$1").trim());
        doc.select("a").remove();
        return doc.text();
    }

    private Optional<String> getFirstSentense(Optional<String> src) {
        String regex = "^[^.!?]*[.!?]";
        Pattern pattern = Pattern.compile(regex);
        return src.map(txt -> {
            Matcher matcher = pattern.matcher(txt);
            if (matcher.find())
                return matcher.group();
            else
                return txt;
        });
    }

    private String checkAddLastDot(String src) {
        String result = src.transform(str -> {
            if (str.charAt(str.length() - 1) != '.')
                return str + ".";
            else
                return str;
        });
        return result;
    }
}
