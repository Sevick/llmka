package com.fbytes.llmka.service.DataSource.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.datasource.DataSource;
import com.fbytes.llmka.model.datasource.DataSourceFactory;
import com.fbytes.llmka.service.DataSource.IDataSourceConfigReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Scanner;
import java.util.function.Consumer;

@Service
public class DataSourceConfigReader implements IDataSourceConfigReader {

    private static final Logger logger = Logger.getLogger(DataSourceFactory.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DataSourceFactory dataSourceFactory;

    @Value("${LLMka.config.ignore_invalid_config:true}")
    private Boolean ignoreInvalidConfig;


    public void retrieveDataSources(InputStream inputStream, Consumer<DataSource> callback) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            long lineNum = 1;
            Scanner inScan = new Scanner(inputStream);
            while (inScan.hasNext()) {
                try {
                    DataSource dataSource = dataSourceFactory.getDataSourceParams(inScan.next());
                    logger.debug("Datasource configuration read: {}", dataSource);
                    //if (StringUtils.isEmpty(dataSource.getId()))
                    //    dataSource.setId(String.format("%d", lineNum));
                    callback.accept(dataSource);
                } catch (Exception e) {
                    logger.logException(String.format("DataSource#%d Exception reading json", lineNum), e);
                    if (!ignoreInvalidConfig)
                        throw e;
                }
                lineNum++;
            }
        }
    }

    public void retrieveDataSourcesFromFile(File inputFile, Consumer<DataSource> callback) {
        try (InputStream inputStream = new FileInputStream(inputFile)) {
            retrieveDataSources(inputStream, callback);
        } catch (Exception e) {
            logger.logException(e); // TODO
        }
    }
}
