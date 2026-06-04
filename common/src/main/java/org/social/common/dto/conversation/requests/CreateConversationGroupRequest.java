package org.social.common.dto.conversation.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationGroupRequest {
    private String name;
    private Set<Integer> memberIds;
    private int userCurrentId;
}
