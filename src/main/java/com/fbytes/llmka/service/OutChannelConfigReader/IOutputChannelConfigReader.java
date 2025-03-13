package com.fbytes.llmka.service.OutChannelConfigReader;

import com.fbytes.llmka.model.heraldchannel.Herald;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public interface IOutputChannelConfigReader {
    void retrieveDataSources(String groupName, InputStream inputStream, Consumer<Herald> callback) throws IOException;

    void retrieveDataSourcesFromFile(File inputFile, Consumer<Herald> callback);
}
