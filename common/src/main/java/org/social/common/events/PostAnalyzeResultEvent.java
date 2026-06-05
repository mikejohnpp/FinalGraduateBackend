package org.social.common.events;

public record PostAnalyzeResultEvent(
        Integer postId,
        String sentiment,
        Double confidence
) {
}
