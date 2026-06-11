package org.social.common.dto.conversation.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponseDetail {

    private int conversationId;
    private String conversationName;
    private boolean isGroup;
    private Instant createdAt;
    private Set<UserResponse>members;
    private Set<MessageResponse> messages;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
