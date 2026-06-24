package org.social.common.events;

public record AnalyzeSentimentEvent(
        String sentence,
        Integer postId,
        String entityType,
        Integer entityId
) {
}
