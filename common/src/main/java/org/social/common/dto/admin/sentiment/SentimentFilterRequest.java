package org.social.common.dto.admin.sentiment;

import java.time.Instant;

public record SentimentFilterRequest(
        String sentiment,
        Instant fromDate,
        Instant toDate,
        Double minConfidence,
        Double maxConfidence,
        String keyword,
        Integer groupId) {
}
