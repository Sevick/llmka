package com.fbytes.llmka.service.DataSource;

import com.fbytes.llmka.model.datasource.DataSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public interface IDataSourceConfigReader {
    void retrieveDataSources(InputStream inputStream, Consumer<DataSource> callback) throws IOException;
    void retrieveDataSourcesFromFile(File inputFile, Consumer<DataSource> callback);
}
