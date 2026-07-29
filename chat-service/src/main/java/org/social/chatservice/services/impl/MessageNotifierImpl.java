package org.social.chatservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.chatservice.messaging.relay.RedisMessageRelay;
import org.social.chatservice.services.ConversationMemberCache;
import org.social.chatservice.services.MessageNotifier;
import org.social.common.dto.conversation.response.ChatMessageResponse;
import org.social.common.dto.conversation.response.MessageNotification;
import org.social.common.entities.Conversation;
import org.social.common.repositories.ConversationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageNotifierImpl implements MessageNotifier {

    /** Destination client đăng ký: /user/queue/messages */
    private static final String USER_QUEUE_MESSAGES = "/queue/messages";

    private final RedisMessageRelay messageRelay;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberCache conversationMemberCache;

    @Override
    public void notifyNewMessage(ChatMessageResponse message) {
        Integer conversationId = message.getConversationId();
        Integer senderId = message.getUser() != null ? message.getUser().getId() : null;

        List<Integer> memberIds = conversationMemberCache.getMemberIds(conversationId);
        if (memberIds.isEmpty()) {
            log.warn("[chat-service] Hội thoại {} không có thành viên, bỏ qua thông báo", conversationId);
            return;
        }

        MessageNotification notification = buildNotification(message, conversationId);

        for (Integer memberId : memberIds) {
            // Người gửi đã thấy tin nhắn trong khung chat, không cần thông báo.
            if (senderId != null && senderId.equals(memberId)) {
                continue;
            }
            messageRelay.sendToUser(
                    String.valueOf(memberId),
                    USER_QUEUE_MESSAGES,
                    notification);
        }
    }

    private MessageNotification buildNotification(ChatMessageResponse message, Integer conversationId) {
        // findById không kéo theo members (ManyToMany lazy) nên chỉ tốn 1 truy vấn nhẹ.
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);

        return new MessageNotification(
                conversationId,
                conversation != null ? conversation.getName() : null,
                conversation != null && Boolean.TRUE.equals(conversation.getIsGroup()),
                message.getId(),
                message.getContent(),
                message.getMessageType(),
                message.getCreatedAt(),
                message.getUser());
    }
}
