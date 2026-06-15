package org.social.userservice.messaging.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.common.entities.Comment;
import org.social.common.entities.Post;
import org.social.common.events.PostAnalyzeResultEvent;
import org.social.common.kafka.support.EventEnvelope;
import org.social.common.repositories.CommentRepository;
import org.social.common.repositories.PostRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostAnalyzeResultListener {

    private final ObjectMapper kafkaObjectMapper;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

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
        log.info("[user-service] postAnalyzeResult entityType={} entityId={} sentiment={} confidence={} eventId={}",
                payload.entityType(), payload.entityId(), payload.sentiment(), payload.confidence(), envelope.eventId());

        if ("COMMENT".equals(payload.entityType())) {
            Comment comment = commentRepository.findByIdAndIsActiveTrue(payload.entityId()).orElse(null);
            if (comment == null) {
                log.warn("[user-service] postAnalyzeResult: Comment {} not found or inactive, skipping", payload.entityId());
                return;
            }
            comment.setSentiment(payload.sentiment());
            comment.setConfidence(payload.confidence());
            commentRepository.save(comment);
        } else {
            Integer id = payload.entityId() != null ? payload.entityId() : payload.postId();
            Post post = postRepository.findByIdAndIsActiveTrue(id).orElse(null);
            if (post == null) {
                log.warn("[user-service] postAnalyzeResult: Post {} not found or inactive, skipping", id);
                return;
            }
            post.setSentiment(payload.sentiment());
            post.setConfidence(payload.confidence());
            postRepository.save(post);
        }
    }

    private void handleSentimentCancelled(EventEnvelope<Object> envelope) {
        PostAnalyzeResultEvent payload = kafkaObjectMapper.convertValue(envelope.payload(), PostAnalyzeResultEvent.class);
        log.warn("[user-service] postAnalyzeCancelled entityType={} entityId={} cancelReason={} eventId={}",
                payload.entityType(), payload.entityId(), payload.cancelReason(), envelope.eventId());

        if ("COMMENT".equals(payload.entityType())) {
            Comment comment = commentRepository.findByIdAndIsActiveTrue(payload.entityId()).orElse(null);
            if (comment == null) {
                log.warn("[user-service] postAnalyzeCancelled: Comment {} not found or inactive, skipping", payload.entityId());
                return;
            }
            comment.setCancelReason(payload.cancelReason());
            commentRepository.save(comment);
        } else {
            Integer id = payload.entityId() != null ? payload.entityId() : payload.postId();
            Post post = postRepository.findByIdAndIsActiveTrue(id).orElse(null);
            if (post == null) {
                log.warn("[user-service] postAnalyzeCancelled: Post {} not found or inactive, skipping", id);
                return;
            }
            post.setCancelReason(payload.cancelReason());
            postRepository.save(post);
        }
    }
}
