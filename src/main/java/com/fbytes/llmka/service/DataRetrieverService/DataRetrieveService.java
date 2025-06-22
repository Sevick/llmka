package com.fbytes.llmka.service.DataRetrieverService;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.config.newssource.NewsSource;
import com.fbytes.llmka.service.DataRetriver.IDataRetriever;
import io.micrometer.core.annotation.Timed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Qualifier("dataRetrieveService")
public class DataRetrieveService implements IDataRetrieveService {
    private static final Logger logger = Logger.getLogger(DataRetrieveService.class);

    private final Map<String, IDataRetriever> retrieverMap;

    public DataRetrieveService(@Autowired Map<String, IDataRetriever> retrieverMap) {
        this.retrieverMap = retrieverMap;
    }

    @Timed(value = "llmka.dataretrive.time", description = "time to retrieve data from newsSource", percentiles = {0.5, 0.9})
    public Optional<Stream<NewsData>> retrieveData(NewsSource newsSource) throws NoSuchRetrieverException {
        IDataRetriever dataRetriever = retrieverMap.get("dataRetriever" + StringUtils.capitalize(newsSource.getType()));
        if (dataRetriever == null) {
            logger.error("No Retriever registered for NewSource.type: {}", newsSource.getType());
            throw new NoSuchRetrieverException();
        }
        return dataRetriever.retrieveData(newsSource);
    }
}
