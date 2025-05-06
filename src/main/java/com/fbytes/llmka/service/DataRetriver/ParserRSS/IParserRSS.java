package com.fbytes.llmka.service.DataRetriver.ParserRSS;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.config.newssource.NewsSource;

import java.io.IOException;
import java.io.InputStream;

public interface IParserRSS {
    NewsData[] parseRSS(InputStream inputStream) throws ParserRSS.ParsingException, IOException;
}
