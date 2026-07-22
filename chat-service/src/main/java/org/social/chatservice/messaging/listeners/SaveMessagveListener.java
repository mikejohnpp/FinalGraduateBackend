package org.social.chatservice.messaging.listeners;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.chatservice.services.ConversationService;
import org.social.common.entities.Conversation;
import org.social.common.entities.Message;
import org.social.common.entities.User;
import org.social.common.events.FriendAcceptedEvent;
import org.social.common.events.SaveMessageEvent;
import org.social.common.kafka.support.EventEnvelope;
import org.social.common.repositories.ConversationRepository;
import org.social.common.repositories.MessageRepository;
import org.social.common.repositories.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveMessagveListener {

    private final ObjectMapper kafkaObjectMapper;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @KafkaListener(topics = "${app.kafka.topics.chat.save}", groupId = "chat-service", containerFactory = "kafkaListenerContainerFactory")
    public void onSaveMessage(EventEnvelope<Object> envelope, Acknowledgment ack) {
        try {
            SaveMessageEvent payload = kafkaObjectMapper.convertValue(envelope.payload(), SaveMessageEvent.class);
            Conversation conversation =
                    conversationRepository.getReferenceById(payload.conversationId());

            User sender =
                    userRepository.getReferenceById(Long.valueOf(payload.senderId()));

            Message message = new Message();
            message.setId(payload.id());
            message.setConversation(conversation);
            message.setSender(sender);
            message.setContent(payload.content());
            message.setIsActive(payload.isActive());
            message.setCreatedAt(payload.createAt());
            message.setMessageType(payload.messageType());

            messageRepository.save(message);
            System.out.println("hello kafla");
        } catch (Exception e) {
            log.error("[chat-service] Lỗi xử lý sự kiện kết bạn (eventId={}): {}",
                    envelope.eventId(), e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }

}
