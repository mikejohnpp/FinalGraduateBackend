package org.social.common.dto.story.views;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.social.common.dto.conversation.response.UserResponse;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoryDTO {

    private int id;
    private String content;
    private String urlImage;
    private String urlVideo;
    private UserResponse user;
    private Instant createdAt;
    private Boolean isActive;
    private String type;
    private String color;
}
