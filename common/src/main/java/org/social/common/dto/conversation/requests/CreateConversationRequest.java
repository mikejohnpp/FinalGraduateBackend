package org.social.common.dto.conversation.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {

//    Chưa biết bên frontend sẽ dùng email bên kia hya ID
    private int userOppenentId;

    private int userCurrentId;

}
