package org.social.userservice.messaging.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.common.events.PostAnalyzeResultEvent;
import org.social.common.kafka.support.EventEnvelope;
import org.social.common.repositories.PostRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.social.common.entities.Post;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostAnalyzeResultListener {

    private final ObjectMapper kafkaObjectMapper;
    private final PostRepository postRepository;

    @KafkaListener(
            topics = "${app.kafka.topics.post.analyze.result}",
            groupId = "user-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPostAnalyzeResult(EventEnvelope<Object> envelope, Acknowledgment ack) {
        String eventType = envelope.eventType();

        if ("postAnalyzeResult".equals(eventType)) {
            handleSentimentResult(envelope);
        } else if ("postAnalyzeCancelled".equals(eventType)) {
            handleSentimentCancelled(envelope);
        } else {
            log.warn("[user-service] Unknown eventType={} on post.analyze.result, skipping", eventType);
        }

        ack.acknowledge();
    }

    private void handleSentimentResult(EventEnvelope<Object> envelope) {
        PostAnalyzeResultEvent payload = kafkaObjectMapper.convertValue(envelope.payload(), PostAnalyzeResultEvent.class);
        log.info("[user-service] postAnalyzeResult postId={} sentiment={} confidence={} eventId={}",
                payload.postId(), payload.sentiment(), payload.confidence(), envelope.eventId());

        Post post = postRepository.findByIdAndIsActiveTrue(payload.postId()).orElse(null);
        if (post == null) {
            log.warn("[user-service] postAnalyzeResult: Post {} not found or inactive, skipping", payload.postId());
            return;
        }
        post.setSentiment(payload.sentiment());
        post.setConfidence(payload.confidence());
        postRepository.save(post);
    }

    private void handleSentimentCancelled(EventEnvelope<Object> envelope) {
        PostAnalyzeResultEvent payload = kafkaObjectMapper.convertValue(envelope.payload(), PostAnalyzeResultEvent.class);
        log.warn("[user-service] postAnalyzeCancelled postId={} cancelReason={} eventId={}",
                payload.postId(), payload.cancelReason(), envelope.eventId());

        Post post = postRepository.findByIdAndIsActiveTrue(payload.postId()).orElse(null);
        if (post == null) {
            log.warn("[user-service] postAnalyzeCancelled: Post {} not found or inactive, skipping", payload.postId());
            return;
        }
        post.setCancelReason(payload.cancelReason());
        postRepository.save(post);
    }
}
