package org.social.chatservice.services;

import org.social.common.dto.conversation.response.ChatMessageResponse;

import java.util.List;

public interface ChatRedisService {

     void saveMessage(ChatMessageResponse message);

    List<ChatMessageResponse> getMessages(Integer conversationId,int limited);
}
