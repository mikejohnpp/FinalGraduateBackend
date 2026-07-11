package org.social.common.kafka;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String DEMO_PING = "demo.ping";
    public static final String DEMO_PONG = "demo.pong";
    public static final String AI_ANALYZE_REQUEST = "ai.analyze.request";

    public static final String POST_ANALYZE_PREPROCESSOR = "dev.post.analyze.preprocessor";
    public static final String POST_ANALYZE_SENTIMENT = "dev.post.analyze.sentiment";
    public static final String POST_ANALYZE_RESULT = "dev.post.analyze.result";

    public static final String NOTIFICATION_CREATED = "dev.notification.created";

    public static final String FRIEND_ACCEPTED = "dev.friend.accepted";

    public static final String DLT_SUFFIX = ".dlt";

}
