package org.social.common.dto.conversation.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private int id;
    private int conversationId;
    private String content;
    private Instant createdAt;
    private int senderId;
    private boolean isActive;
}
