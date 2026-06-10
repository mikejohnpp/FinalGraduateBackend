package org.social.chatservice.services.impl;


import lombok.RequiredArgsConstructor;
import org.social.chatservice.services.MessageService;
import org.social.common.dto.conversation.mappers.UserResponseMapper;
import org.social.common.dto.conversation.requests.ChatMessageRequest;
import org.social.common.dto.conversation.response.ChatMessageResponse;
import org.social.common.entities.Conversation;
import org.social.common.entities.Message;
import org.social.common.entities.User;
import org.social.common.exceptions.BusinessException;
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

//        boolean isMember = conversation.getUser().stream()
//                .anyMatch(m -> m.getId() ==sender.getId());
//
//        if (!isMember) {
//            throw new IllegalArgumentException("Sender không phải thành viên của conversation!");
//        }
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(chatMessage.getContent());
        message.setIsActive(true);
        message.setCreatedAt(Instant.now());

        Message saved = messageRepository.save(message);

        return new ChatMessageResponse(
                saved.getId(),
                saved.getContent(),
                saved.getCreatedAt(),
                userResponseMapper.toDTO(sender),
                conversation.getId()
        );

    }
}
