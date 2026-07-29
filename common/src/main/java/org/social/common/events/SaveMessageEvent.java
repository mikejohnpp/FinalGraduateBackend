package org.social.common.events;


import org.social.common.entities.MessageType;

import java.time.Instant;

public record SaveMessageEvent(Long id, Integer conversationId, Integer senderId, String content, Boolean isActive,
                               Instant createAt, MessageType messageType) {

}
