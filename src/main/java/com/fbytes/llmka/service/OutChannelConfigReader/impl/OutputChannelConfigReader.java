package com.fbytes.llmka.service.OutChannelConfigReader.impl;

import com.fbytes.llmka.model.heraldchannel.Herald;
import com.fbytes.llmka.service.OutChannelConfigReader.IOutputChannelConfigReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class OutputChannelConfigReader implements IOutputChannelConfigReader {
    @Override
    public void retrieveDataSources(String groupName, InputStream inputStream, Consumer<Herald> callback) throws IOException {

    }

    @Override
    public void retrieveDataSourcesFromFile(File inputFile, Consumer<Herald> callback) {

    }
}
