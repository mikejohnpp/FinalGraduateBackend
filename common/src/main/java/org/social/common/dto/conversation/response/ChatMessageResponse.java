package org.social.common.dto.conversation.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.social.common.entities.MessageType;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String content;
    private Instant createdAt;
    private UserResponse user;
    private int conversationId;
    private MessageType messageType;
    private Integer callDuration;
    private String tempId;
    private Boolean isActive;
}
