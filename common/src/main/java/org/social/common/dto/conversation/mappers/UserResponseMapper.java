package org.social.common.dto.conversation.mappers;


import lombok.RequiredArgsConstructor;
import org.social.common.dto.conversation.response.UserResponse;
import org.social.common.entities.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResponseMapper {

    public UserResponse toDTO(User user){

        if(user == null){
            return null;
        }
        return new UserResponse(user.getId(), user.getUserName(),user.getAvatar());

    }
}
