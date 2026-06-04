package org.social.common.dto.conversation.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequest {
    private Integer id;
    private Integer conversationId;
    private String content;
    private Instant createdAt;
    private Integer senderId;
    private Boolean isActive;
}
