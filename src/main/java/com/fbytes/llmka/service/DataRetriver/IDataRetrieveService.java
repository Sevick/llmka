package com.fbytes.llmka.service.DataRetriver;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.newssource.NewsSource;

import java.util.Optional;
import java.util.stream.Stream;


public interface IDataRetrieveService {
    public class NoSuchRetrieverException extends Exception {
        public NoSuchRetrieverException() {
        }

        public NoSuchRetrieverException(String message) {
            super(message);
        }
    }

    public Optional<Stream<NewsData>> retrieveData(NewsSource newsSource) throws NoSuchRetrieverException;
}
