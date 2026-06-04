package org.social.common.dto.conversation.mappers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.conversation.response.ConversationResponse;
import org.social.common.dto.conversation.response.UserResponse;
import org.social.common.entities.Conversation;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class ConversationResponseMapper {

    private final UserResponseMapper userResponseMapper;


    public ConversationResponse toDTO(Conversation conversation) {
        if(conversation == null) return null;

        Set<UserResponse> members = conversation.getUser().stream().map(userResponseMapper::toDTO).collect(Collectors.toSet());


        return new ConversationResponse(
                conversation.getId(),
                conversation.getName(),
                conversation.getIsGroup(),
                conversation.getCreatedAt(),
                conversation.getIsActive(),
                members
        );

    }
}
