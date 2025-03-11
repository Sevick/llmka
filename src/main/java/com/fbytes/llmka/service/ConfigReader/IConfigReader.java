package com.fbytes.llmka.service.ConfigReader;

import com.fbytes.llmka.model.IConfigFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public interface IConfigReader<T> {
    List<T> retrieve(IConfigFactory<T> configFactory, String groupName, InputStream inputStream) throws IOException;

    List<T> retrieveFromFile(IConfigFactory<T> configFactory, File inputFile);
}
