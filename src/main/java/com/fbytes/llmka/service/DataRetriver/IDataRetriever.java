package com.fbytes.llmka.service.DataRetriver;

import com.fbytes.llmka.model.datasource.DataSource;
import com.fbytes.llmka.model.NewsData;

import java.util.Optional;
import java.util.stream.Stream;

@FunctionalInterface
public interface IDataRetriever<T extends DataSource> {
    Optional<Stream<NewsData>> retrieveData(T dataSource);
}
