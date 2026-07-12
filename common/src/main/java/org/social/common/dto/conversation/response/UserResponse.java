package org.social.common.dto.conversation.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse implements Serializable {

    private int id;
    private String username;
    private String avatarUrl;

}
