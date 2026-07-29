package org.social.common.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public class KafkaCommonProperties {

    private Topics topics = new Topics();
    private Retry retry = new Retry();

    public Topics getTopics() {
        return topics;
    }

    public void setTopics(Topics topics) {
        this.topics = topics;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public static class Topics {
        private String demoPing = "demo.ping";
        private String demoPong = "demo.pong";
        private String aiAnalyzeRequest = "dev.post.analyze.preprocessor";
        private String saveMessage = "dev.mess.saved";

        public String getDemoPing() { return demoPing; }
        public void setDemoPing(String v) { this.demoPing = v; }
        public String getDemoPong() { return demoPong; }
        public void setDemoPong(String v) { this.demoPong = v; }
        public String getAiAnalyzeRequest() { return aiAnalyzeRequest; }
        public void setAiAnalyzeRequest(String v) { this.aiAnalyzeRequest = v; }

        public String getSaveMessage() {
            return saveMessage;
        }

        public void setSaveMessage(String saveMessage) {
            this.saveMessage = saveMessage;
        }
    }

    public static class Retry {
        private long backoffMs = 1000L;
        private long maxAttempts = 3L;

        public long getBackoffMs() { return backoffMs; }
        public void setBackoffMs(long v) { this.backoffMs = v; }
        public long getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(long v) { this.maxAttempts = v; }
    }
}
