package org.social.common.events;

public record AnalyzeSentimentEvent(
        String sentence,
        String subject,
        String predicate,
        String object
) {
}
