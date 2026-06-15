package org.social.common.events;

public record PostAnalyzeResultEvent(
        Integer postId,
        String entityType,
        Integer entityId,
        String sentiment,
        Double confidence,
        String cancelReason
) {
}
