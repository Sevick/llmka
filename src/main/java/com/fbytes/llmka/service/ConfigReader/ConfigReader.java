package com.fbytes.llmka.service.ConfigReader;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.config.IConfigFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class ConfigReader<T> implements IConfigReader<T> {
    private static final Logger logger = Logger.getLogger(ConfigReader.class);

    protected final boolean ignoreInvalidConfig;

    public ConfigReader(Boolean ignoreInvalidConfig) {
        this.ignoreInvalidConfig = ignoreInvalidConfig;
    }

    @Override
    public List<T> retrieve(IConfigFactory<T> configFactory, InputStream inputStream) throws IOException {
        List<T> result = new ArrayList<>();
        retrieve(configFactory, inputStream, item -> result.add(item));
        return result;
    }

    @Override
    public void retrieve(IConfigFactory<T> configFactory, InputStream inputStream, Consumer<T> callback) throws IOException {
        try (Scanner inScan = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            long lineNum = 1;
            while (inScan.hasNext()) {
                try {
                    T configData = configFactory.getParams(inScan.nextLine());
                    logger.debug("{} configuration read: {}", configData.getClass().getName(), configData);
                    callback.accept(configData);
                } catch (Exception e) {
                    logger.logException(String.format("#%d Exception reading config json", lineNum), e);
                    if (!ignoreInvalidConfig)
                        throw e;
                }
                lineNum++;
            }
        }
    }

    @Override
    public List<T> retrieveFromFile(IConfigFactory<T> configFactory, File inputFile) {
        try (InputStream inputStream = new FileInputStream(inputFile)) {
            return retrieve(configFactory, inputStream);
        } catch (Exception e) {
            logger.logException(e); // TODO
            if (!ignoreInvalidConfig)
                throw new RuntimeException(e);
            return null;
        }
    }

    @Override
    public void retrieveFromFile(IConfigFactory<T> configFactory, File inputFile, Consumer<T> callback) {
        try (InputStream inputStream = new FileInputStream(inputFile)) {
            retrieve(configFactory, inputStream, callback);
        } catch (Exception e) {
            logger.logException(e); // TODO
            if (!ignoreInvalidConfig)
                throw new RuntimeException(e);
        }
    }
}
