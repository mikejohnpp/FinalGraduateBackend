package org.social.userservice.messaging.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.common.events.PingEvent;
import org.social.common.events.PongEvent;
import org.social.common.kafka.config.KafkaCommonProperties;
import org.social.common.kafka.support.EventEnvelope;
import org.social.common.kafka.support.EventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class PingListener {

    private final EventPublisher publisher;
    private final KafkaCommonProperties properties;
    private final ObjectMapper kafkaObjectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.demo-ping}",
            groupId = "user-service-demo",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPing(EventEnvelope<Object> envelope, Acknowledgment ack) {
        PingEvent payload = kafkaObjectMapper.convertValue(envelope.payload(), PingEvent.class);
        log.info("[user-service] received PING from={} message='{}' sentAt={} (eventId={})",
                payload.from(),
                payload.message(),
                payload.sentAt(),
                envelope.eventId());

        PongEvent reply = new PongEvent(
                "user-service",
                payload.from(),
                "pong: " + payload.message(),
                Instant.now()
        );
        publisher.publish(
                properties.getTopics().getDemoPong(),
                reply.replyTo(),
                "demo.pong",
                reply
        );
        log.info("[user-service] sent PONG replyTo={}", reply.replyTo());

        ack.acknowledge();
    }
}
