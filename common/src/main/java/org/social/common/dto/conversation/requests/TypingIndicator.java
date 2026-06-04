package org.social.common.dto.conversation.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicator {
    private int conversationId;
    private int senderId;
    private boolean isTyping;
}
