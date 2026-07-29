package org.social.chatservice.services;

import org.social.common.dto.conversation.response.ChatMessageResponse;

public interface MessageNotifier {

    void notifyNewMessage(ChatMessageResponse message);
}
