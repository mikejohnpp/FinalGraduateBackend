package org.social.common.dto.conversation.mappers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.conversation.response.MessageResponse;
import org.social.common.dto.conversation.response.UserResponse;
import org.social.common.entities.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageResponseMapper {
    private final UserResponseMapper userResponseMapper;

    public MessageResponse toDTO(Message message) {

        if (message == null) {
            return null;
        }

        UserResponse user = userResponseMapper.toDTO(message.getSender());

        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getContent(),
                message.getCreatedAt(),
                user,
                message.getIsActive(),
                message.getMessageType(),
                message.getCallDuration());
    }
}
