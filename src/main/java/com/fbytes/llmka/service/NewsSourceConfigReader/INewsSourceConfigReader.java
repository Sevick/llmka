package com.fbytes.llmka.service.NewsSourceConfigReader;

import com.fbytes.llmka.model.newssource.NewsSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public interface INewsSourceConfigReader {
    void retrieveNewsSources(String groupName, InputStream inputStream, Consumer<NewsSource> callback) throws IOException;
    void retrieveNewsSourcesFromFile(File inputFile, Consumer<NewsSource> callback);
}
