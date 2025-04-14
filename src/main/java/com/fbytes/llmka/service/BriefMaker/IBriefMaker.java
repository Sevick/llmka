package com.fbytes.llmka.service.BriefMaker;

import com.fbytes.llmka.model.NewsData;

public interface IBriefMaker {

    NewsData makeBrief(NewsData newsData);
}
