package com.fbytes.llmka.service.ConfigReader;

import com.fbytes.llmka.model.IConfigFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public interface IConfigReader<T> {
    List<T> retrieve(IConfigFactory<T> configFactory, InputStream inputStream) throws IOException;

    void retrieve(IConfigFactory<T> configFactory, InputStream inputStream, Consumer<T> callback) throws IOException;

    List<T> retrieveFromFile(IConfigFactory<T> configFactory, File inputFile);

    void retrieveFromFile(IConfigFactory<T> configFactory, File inputFile, Consumer<T> callback);
}
