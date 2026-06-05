package org.social.common.events;

public record AnalyzeSentimentEvent(
        String sentence,
        Integer postId
) {
}
