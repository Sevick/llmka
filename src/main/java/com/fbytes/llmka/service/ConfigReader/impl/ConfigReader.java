package com.fbytes.llmka.service.ConfigReader.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.IConfigFactory;
import com.fbytes.llmka.service.ConfigReader.IConfigReader;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConfigReader<T> implements IConfigReader<T> {
    @Value("${llmka.config.ignore_invalid_config:true}")
    private Boolean ignoreInvalidConfig;

    private static final Logger logger = Logger.getLogger(ConfigReader.class);


    @Override
    public List<T> retrieve(IConfigFactory<T> configFactory, String groupName, InputStream inputStream) throws IOException {
        List<T> result = new ArrayList<>();
        try (Scanner inScan = new Scanner(inputStream)) {
            long lineNum = 1;
            while (inScan.hasNext()) {
                try {
                    T configData = configFactory.getParams(inScan.next());
                    logger.debug("{} configuration read: {}", configData.getClass().getName(), configData);
                    result.add(configData);
                } catch (Exception e) {
                    logger.logException(String.format("#%d Exception reading config json", lineNum), e);
                    if (!ignoreInvalidConfig)
                        throw e;
                }
                lineNum++;
            }
        }
        return result;
    }

    @Override
    public List<T> retrieveFromFile(IConfigFactory<T> configFactory, File inputFile) {
        try (InputStream inputStream = new FileInputStream(inputFile)) {
            return retrieve(configFactory, inputFile.getName(), inputStream);
        } catch (Exception e) {
            logger.logException(e); // TODO
            if (!ignoreInvalidConfig)
                throw new RuntimeException(e);
            return null;
        }
    }
}
