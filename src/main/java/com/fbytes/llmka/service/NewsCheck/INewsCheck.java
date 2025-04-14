package com.fbytes.llmka.service.NewsCheck;

import com.fbytes.llmka.model.NewsData;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;

public interface INewsCheck {
    Optional<RejectReason> checkNews(String schema, NewsData newsData);

    @Data
    @AllArgsConstructor
    public class RejectReason {
        public enum REASON {META_DUPLICATION, CLOSE_MATCH, COMMERCIAL}

        private RejectReason.REASON reason;
        private String explain;

        public RejectReason(INewsCheck.RejectReason.REASON reason) {
            this.reason = reason;
        }
    }
}
