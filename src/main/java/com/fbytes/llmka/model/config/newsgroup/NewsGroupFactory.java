package com.fbytes.llmka.model.config.newsgroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.config.IConfigFactory;
import org.springframework.stereotype.Service;

@Service
public class NewsGroupFactory implements IConfigFactory<NewsGroup> {
    private static final Logger logger = Logger.getLogger(NewsGroupFactory.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public NewsGroup getParams(String json) throws JsonProcessingException {
        return mapper.readValue(json, NewsGroup.class);
    }
}
