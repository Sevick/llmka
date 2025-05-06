package com.fbytes.llmka.service.NewsProcessor;

import com.fbytes.llmka.model.NewsData;

public interface INewsProcessor {

    NewsData process(NewsData newsData);
}
