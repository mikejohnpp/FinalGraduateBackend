package org.social.common.kafka.support;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        String eventId,
        String eventType,
        String traceId,
        Instant occurredAt,
        String source,
        int version,
        T payload
) {

    public static <T> EventEnvelope<T> of(String eventType, String source, T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID().toString(),
                eventType,
                null,
                Instant.now(),
                source,
                1,
                payload
        );
    }

    public static <T> EventEnvelope<T> of(String eventType, String source, int version, T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID().toString(),
                eventType,
                null,
                Instant.now(),
                source,
                version,
                payload
        );
    }
}
