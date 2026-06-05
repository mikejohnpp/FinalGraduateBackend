package org.social.userservice.messaging.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.common.kafka.support.EventEnvelope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.social.common.events.PostAnalyzeResultEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostAnalyzeResultListener {

    private final ObjectMapper kafkaObjectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.post.analyze.result}",
            groupId = "user-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPong(EventEnvelope<Object> envelope, Acknowledgment ack) {
        PostAnalyzeResultEvent payload = kafkaObjectMapper.convertValue(envelope.payload(), PostAnalyzeResultEvent.class);
        log.info("[user-service] received PostAnalyzeResultEvent with postId={} sentiment={} confidence={} eventId={}",
                payload.postId(),
                payload.sentiment(),
                payload.confidence(),
                envelope.eventId());
        ack.acknowledge();
    }
}
