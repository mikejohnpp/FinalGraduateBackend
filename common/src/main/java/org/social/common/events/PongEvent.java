package org.social.common.events;

import java.time.Instant;

public record PongEvent(
        String from,
        String replyTo,
        String message,
        Instant sentAt
) {
}
