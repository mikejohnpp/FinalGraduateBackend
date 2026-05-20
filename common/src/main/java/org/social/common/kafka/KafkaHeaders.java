package org.social.common.kafka;

public final class KafkaHeaders {

    private KafkaHeaders() {
    }

    public static final String EVENT_ID = "X-Event-Id";
    public static final String EVENT_TYPE = "X-Event-Type";
    public static final String EVENT_VERSION = "X-Event-Version";
    public static final String EVENT_SOURCE = "X-Event-Source";
    public static final String TRACE_ID = "X-Trace-Id";
    public static final String OCCURRED_AT = "X-Occurred-At";
}
