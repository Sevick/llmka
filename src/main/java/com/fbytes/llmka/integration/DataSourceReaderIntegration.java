package com.fbytes.llmka.integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.DataSource.IDataSourceConfigReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class DataSourceReaderIntegration {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private IDataSourceConfigReader dataSourceConfigReader;

    private static final Logger logger = Logger.getLogger(DataSourceReaderIntegration.class);

    public void readDataSourceConfig(InputStream inputStream) {
        MessageChannel datasourceChannel = applicationContext.getBean("datasourceChannel", MessageChannel.class);
        try {
            dataSourceConfigReader.retrieveDataSources(inputStream, item ->
                    datasourceChannel.send(MessageBuilder.withPayload(item).build()));
        } catch (Exception e) {
            logger.logException(e);                 // TODO - send message / circuit breaker
        }
    }
}
