package org.social.common.dto.conversation.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse implements Serializable {
    private Integer id;
    private String name;
    private boolean isGroup;
    private Instant createdAt;
    private boolean isActive;
    private Set<UserResponse> members;
}
