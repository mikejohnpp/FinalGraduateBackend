package org.social.chatservice.messaging.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.chatservice.services.ConversationService;
import org.social.common.events.FriendAcceptedEvent;
import org.social.common.kafka.support.EventEnvelope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Lắng nghe sự kiện kết bạn thành công (phát từ user-service) và tạo sẵn cuộc
 * trò chuyện 1-1 giữa hai người. Idempotent nhờ ensurePrivateConversation nên
 * an toàn với việc tiêu thụ lặp lại.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FriendAcceptedListener {

    private final ObjectMapper kafkaObjectMapper;
    private final ConversationService conversationService;

    @KafkaListener(topics = "${app.kafka.topics.friend.accepted}", groupId = "chat-service", containerFactory = "kafkaListenerContainerFactory")
    public void onFriendAccepted(EventEnvelope<Object> envelope, Acknowledgment ack) {
        try {
            FriendAcceptedEvent payload = kafkaObjectMapper.convertValue(envelope.payload(), FriendAcceptedEvent.class);
            conversationService.ensurePrivateConversation(payload.userAId(), payload.userBId());
        } catch (Exception e) {
            log.error("[chat-service] Lỗi xử lý sự kiện kết bạn (eventId={}): {}",
                    envelope.eventId(), e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
