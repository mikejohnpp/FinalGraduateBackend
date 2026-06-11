package org.social.common.dto.conversation.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private int id;
    private String username;
    private String avatarUrl;

}
