package com.fbytes.llmka.service.NewsSourceConfigReader.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.newssource.NewsSource;
import com.fbytes.llmka.model.newssource.NewsSourceFactory;
import com.fbytes.llmka.service.NewsSourceConfigReader.INewsSourceConfigReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Scanner;
import java.util.function.Consumer;


//TODO: Replace with generic config reader
@Service
public class NewsSourceConfigReader implements INewsSourceConfigReader {

    private static final Logger logger = Logger.getLogger(NewsSourceFactory.class);

    @Autowired
    private NewsSourceFactory newsSourceFactory;

    @Value("${llmka.config.ignore_invalid_config:true}")
    private Boolean ignoreInvalidConfig;


    @Override
    public void retrieveNewsSources(String groupName, InputStream inputStream, Consumer<NewsSource> callback) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            long lineNum = 1;
            Scanner inScan = new Scanner(inputStream);
            while (inScan.hasNext()) {
                try {
                    NewsSource newsSource = newsSourceFactory.getParams(inScan.next());
                    logger.debug("NewsSource configuration read: {}", newsSource);
                    if (StringUtils.isEmpty(newsSource.getId()))
                        newsSource.setId(String.format("%d", lineNum));
                    callback.accept(newsSource);
                } catch (Exception e) {
                    logger.logException(String.format("NewsSource#%d Exception reading json", lineNum), e);
                    if (!ignoreInvalidConfig)
                        throw e;
                }
                lineNum++;
            }
        }
    }


    public void retrieveNewsSourcesFromFile(File inputFile, Consumer<NewsSource> callback) {
        try (InputStream inputStream = new FileInputStream(inputFile)) {
            retrieveNewsSources(inputFile.getName(), inputStream, callback);
        } catch (Exception e) {
            logger.logException(e); // TODO
        }
    }
}
