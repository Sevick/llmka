package com.fbytes.llmka.service.DataRetriver;

import com.fbytes.llmka.model.newssource.NewsSource;
import com.fbytes.llmka.model.NewsData;

import java.util.Optional;
import java.util.stream.Stream;

@FunctionalInterface
public interface IDataRetriever<T extends NewsSource> {
    Optional<Stream<NewsData>> retrieveData(T dataSource);
}
