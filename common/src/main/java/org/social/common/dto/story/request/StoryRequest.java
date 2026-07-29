package org.social.common.dto.story.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryRequest {
    private Integer userId;
    private String content;
    private String urlImage;
    private String urlVideo;
    private String type;
    private String color;
}
