package org.social.chatservice.services;

import org.social.common.dto.conversation.requests.ChatMessageRequest;
import org.social.common.dto.conversation.response.ChatMessageResponse;
import org.social.common.entities.MessageType;

public interface MessageService {

    ChatMessageResponse saveMessage(ChatMessageRequest chatMessage);

    ChatMessageResponse saveCallEndedMessage(Integer conversationId, Long senderId, MessageType type, Integer durationSeconds);
}
