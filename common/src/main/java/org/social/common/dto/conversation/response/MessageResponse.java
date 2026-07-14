package org.social.common.dto.conversation.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.social.common.entities.MessageType;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private int conversationId;
    private String content;
    private Instant createdAt;
    private UserResponse user;
    private Boolean isActive;
    private MessageType messageType;
    private Integer callDuration;
}
