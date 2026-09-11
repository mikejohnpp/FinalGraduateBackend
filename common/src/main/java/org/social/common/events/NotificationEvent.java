package org.social.common.events;

public record NotificationEvent(
        Integer recipientId,
        Integer actorId,
        String type,
        String entityType,
        Integer entityId,
        String message,
        String link) {
}
