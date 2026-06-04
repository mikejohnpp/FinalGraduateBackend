package org.social.chatservice.services;

import org.social.common.dto.conversation.requests.ChatMessageRequest;
import org.social.common.dto.conversation.response.ChatMessageResponse;

public interface MessageService {

    ChatMessageResponse saveMessage(ChatMessageRequest chatMessage);
}
