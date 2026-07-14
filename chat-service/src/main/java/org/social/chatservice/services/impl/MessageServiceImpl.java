package org.social.chatservice.services.impl;


import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.social.chatservice.messaging.publishers.SaveMessagePublishers;
import org.social.chatservice.services.MessageService;
import org.social.common.dto.conversation.mappers.UserResponseMapper;
import org.social.common.dto.conversation.requests.ChatMessageRequest;
import org.social.common.dto.conversation.response.ChatMessageResponse;
import org.social.common.entities.Conversation;
import org.social.common.entities.Message;
import org.social.common.entities.MessageType;
import org.social.common.entities.User;
import org.social.common.events.SaveMessageEvent;
import org.social.common.repositories.ConversationRepository;
import org.social.common.repositories.MessageRepository;
import org.social.common.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;
    private final ChatRedisServiceImpl chatRedisService;
    private final SaveMessagePublishers saveMessagePublishers;

    @Override
    public ChatMessageResponse saveMessage(ChatMessageRequest chatMessage) {
        User sender = userRepository.findById(Long.valueOf(chatMessage.getSenderId()))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người gửi: " + chatMessage.getSenderId()));

//        Conversation conversation = conversationRepository.findById(chatMessage.getConversationId())
//                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng: " + chatMessage.getConversationId()));

        Message message = new Message();
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        long id = snowflake.nextId();
        message.setId(id);
//        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(chatMessage.getContent());
        message.setIsActive(true);
        message.setCreatedAt(Instant.now());
        message.setMessageType(chatMessage.getMessageType());

        ChatMessageResponse response = new ChatMessageResponse(
                message.getId(),
                message.getContent(),
                message.getCreatedAt(),
                userResponseMapper.toDTO(sender),
                chatMessage.getConversationId(),
                message.getMessageType(),
                message.getCallDuration(),
                chatMessage.getTempId(),
                message.getIsActive()
        );
        chatRedisService.saveMessage(response);

        saveMessagePublishers.sendMessage(new SaveMessageEvent(id,chatMessage.getConversationId(),sender.getId(),message.getContent(),message.getIsActive(),message.getCreatedAt(),message.getMessageType()));
//        messageRepository.save(message);


        return response;
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
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);

        long id = snowflake.nextId();
        message.setId(id);
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
                saved.getCallDuration(),
                null,
                saved.getIsActive()
        );
    }
}
