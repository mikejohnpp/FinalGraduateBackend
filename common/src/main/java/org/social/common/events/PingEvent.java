package org.social.common.events;

import java.time.Instant;

public record PingEvent(
        String from,
        String message,
        Instant sentAt
) {
}
