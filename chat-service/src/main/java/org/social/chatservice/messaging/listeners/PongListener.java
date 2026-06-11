package org.social.chatservice.messaging.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.common.events.PongEvent;
import org.social.common.kafka.support.EventEnvelope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PongListener {

    private final ObjectMapper kafkaObjectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.demo-pong}",
            groupId = "chat-service-demo",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPong(EventEnvelope<Object> envelope, Acknowledgment ack) {
        PongEvent payload = kafkaObjectMapper.convertValue(envelope.payload(), PongEvent.class);
        log.info("[chat-service] received PONG from={} replyTo={} message='{}' sentAt={} (eventId={})",
                payload.from(),
                payload.replyTo(),
                payload.message(),
                payload.sentAt(),
                envelope.eventId());
        ack.acknowledge();
    }
}
