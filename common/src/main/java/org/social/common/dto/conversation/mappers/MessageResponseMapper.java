package org.social.common.dto.conversation.mappers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.conversation.response.MessageResponse;
import org.social.common.entities.Message;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class MessageResponseMapper {
    public MessageResponse toDTO(Message message) {

        if(message == null) {
            return null;
        }

        return new MessageResponse(
                message.getId(),
               message.getConversation().getId(),
                message.getContent(),
                message.getCreatedAt(),
                message.getSender().getId(),
                message.getIsActive()
        );
    }
}
