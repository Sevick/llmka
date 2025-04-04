package com.fbytes.llmka.service.DataRetriver;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.newssource.NewsSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Qualifier("DataRetrieveService")
public class DataRetrieveService implements IDataRetrieveService {
    private static final Logger logger = Logger.getLogger(DataRetrieveService.class);

    @Autowired
    private Map<String, IDataRetriever> retrieverMap;

    public Optional<Stream<NewsData>> retrieveData(NewsSource newsSource) throws NoSuchRetrieverException {
        IDataRetriever dataRetriever = retrieverMap.get("dataRetriever" + StringUtils.capitalize(newsSource.getType()));
        if (dataRetriever == null) {
            logger.error("No Retriever registered for NewSource.type: {}", newsSource.getType());
            throw new NoSuchRetrieverException();
        }
        return dataRetriever.retrieveData(newsSource);
    }
}
