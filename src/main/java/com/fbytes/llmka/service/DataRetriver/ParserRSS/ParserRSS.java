package com.fbytes.llmka.service.DataRetriver.ParserRSS;


import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.config.newssource.NewsSource;
import com.fbytes.llmka.tools.TextUtil;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ParserRSS implements IParserRSS {
    private static final Logger logger = Logger.getLogger(ParserRSS.class);

    public NewsData[] parseRSS(InputStream inputStream) throws ParsingException, IOException {
        NodeList itemList;
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            doc.getDocumentElement().normalize();
            itemList = doc.getElementsByTagName("item");
        } catch (ParserConfigurationException | SAXException e) {
            throw new ParsingException(e);
        }

        List<NewsData> result = new ArrayList<>();
        for (int i = 0; i < itemList.getLength(); i++) {
            Node itemNode = itemList.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) itemNode;
                Optional<String> guid = Optional.ofNullable(itemElement.getElementsByTagName("guid").item(0))
                        .map(item -> item.getTextContent());
                guid = guid.or(() ->
                        Optional.ofNullable(itemElement.getElementsByTagName("id").item(0))
                                .map(item -> item.getTextContent())
                );
                String title = itemElement.getElementsByTagName("title").item(0).getTextContent()
                        .transform(txt -> cleanupString(txt));
                String link = itemElement.getElementsByTagName("link").item(0).getTextContent();
                Optional<String> description = Optional.ofNullable(itemElement.getElementsByTagName("description").item(0))
                        .map(el -> parseElement(el));
                Optional<String> fullText = Optional.ofNullable(itemElement.getElementsByTagName("full-text").item(0))
                        .map(el -> parseElement(el));
                Optional<String> content = Optional.ofNullable(itemElement.getElementsByTagName("content").item(0))
                        .map(el -> parseElement(el));

                if (description.isEmpty() || description.get().isEmpty()) {
                    description = fullText.or(() -> content);
                }
                title = title.transform(txt -> TextUtil.checkAddLastDot(TextUtil.trimTail(txt)));
                description = description.map(txt -> TextUtil.trimTail(txt));
                String extId = guid.orElse(link);

                Optional<Timestamp> pubDate;
                try {
                    pubDate = Optional.ofNullable(itemElement.getElementsByTagName("pubDate").item(0))
                            .map(item -> item.getTextContent())
                            .map(dateTxt -> {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                                LocalDateTime localDateTime = LocalDateTime.parse(dateTxt, formatter);
                                return Timestamp.valueOf(localDateTime);
                            });
                } catch (Exception e) {
                    pubDate = Optional.empty();
                }

                result.add(
                        NewsData.builder()
                                .id(extId)
                                .link(link)
                                .title(title)
                                .description(description)
                                .text(Optional.empty())
                                .pubDate(pubDate)
                                .fetchDate(new Timestamp(System.currentTimeMillis()))
                                .build()
                );
            }
        }
        return result.toArray(NewsData[]::new);
    }


    private String cleanupString(String src) {
        return src.trim().replaceAll("—", "-");
    }

    private String parseElement(Node src) {
        org.jsoup.nodes.Document doc = Jsoup.parse(src.getTextContent().replaceAll("<!\\[CDATA\\[(.*)\\]\\]>", "$1").trim());
        doc.select("a").remove();
        String result = cleanupString(doc.text());
        return result;
    }

    public class ParsingException extends Exception {
        public ParsingException() {
        }

        public ParsingException(String message) {
            super(message);
        }

        public ParsingException(Throwable cause) {
            super(cause);
        }
    }
}
