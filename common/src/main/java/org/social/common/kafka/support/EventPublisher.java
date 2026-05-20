package org.social.common.kafka.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.social.common.kafka.KafkaHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String source;

    public <T> CompletableFuture<SendResult<String, Object>> publish(String topic, String key, EventEnvelope<T> envelope) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, envelope);
        record.headers().add(new RecordHeader(KafkaHeaders.EVENT_ID, envelope.eventId().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader(KafkaHeaders.EVENT_TYPE, envelope.eventType().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader(KafkaHeaders.EVENT_SOURCE, envelope.source().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader(KafkaHeaders.EVENT_VERSION, String.valueOf(envelope.version()).getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader(KafkaHeaders.OCCURRED_AT, envelope.occurredAt().toString().getBytes(StandardCharsets.UTF_8)));

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event {} to topic {} (key={}): {}",
                        envelope.eventType(), topic, key, ex.getMessage(), ex);
            } else if (log.isDebugEnabled()) {
                log.debug("Published event {} to {} partition {} offset {}",
                        envelope.eventType(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
        return future;
    }

    public <T> CompletableFuture<SendResult<String, Object>> publish(String topic, String key, String eventType, T payload) {
        return publish(topic, key, EventEnvelope.of(eventType, source, payload));
    }
}
