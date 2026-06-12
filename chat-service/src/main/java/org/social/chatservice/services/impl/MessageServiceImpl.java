package org.social.chatservice.services.impl;


import lombok.RequiredArgsConstructor;
import org.social.chatservice.services.MessageService;
import org.social.common.dto.conversation.mappers.UserResponseMapper;
import org.social.common.dto.conversation.requests.ChatMessageRequest;
import org.social.common.dto.conversation.response.ChatMessageResponse;
import org.social.common.entities.Conversation;
import org.social.common.entities.Message;
import org.social.common.entities.MessageType;
import org.social.common.entities.User;
import org.social.common.repositories.ConversationRepository;
import org.social.common.repositories.MessageRepository;
import org.social.common.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;

    @Override
    public ChatMessageResponse saveMessage(ChatMessageRequest chatMessage) {
        User sender = userRepository.findById(Long.valueOf(chatMessage.getSenderId()))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người gửi: " + chatMessage.getSenderId()));

        Conversation conversation = conversationRepository.findById(chatMessage.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng: " + chatMessage.getConversationId()));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(chatMessage.getContent());
        message.setIsActive(true);
        message.setCreatedAt(Instant.now());
        message.setMessageType(MessageType.TEXT);

        Message saved = messageRepository.save(message);

        return new ChatMessageResponse(
                saved.getId(),
                saved.getContent(),
                saved.getCreatedAt(),
                userResponseMapper.toDTO(sender),
                conversation.getId(),
                saved.getMessageType(),
                saved.getCallDuration()
        );
    }

    @Override
    public ChatMessageResponse saveCallEndedMessage(Integer conversationId, Long senderId, MessageType type, Integer durationSeconds) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người gửi: " + senderId));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng: " + conversationId));

        String content;
        if (durationSeconds == null || durationSeconds == 0) {
            content = "Cuộc gọi nhỡ";
        } else if (type == MessageType.VIDEO_CALL) {
            content = "Cuộc gọi video đã kết thúc";
        } else {
            content = "Cuộc gọi thoại đã kết thúc";
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message.setIsActive(true);
        message.setCreatedAt(Instant.now());
        message.setMessageType(type);
        message.setCallDuration(durationSeconds);

        Message saved = messageRepository.save(message);

        return new ChatMessageResponse(
                saved.getId(),
                saved.getContent(),
                saved.getCreatedAt(),
                userResponseMapper.toDTO(sender),
                conversation.getId(),
                saved.getMessageType(),
                saved.getCallDuration()
        );
    }
}
