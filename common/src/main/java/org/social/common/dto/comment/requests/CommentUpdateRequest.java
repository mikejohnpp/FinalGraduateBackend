package org.social.common.dto.comment.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.social.common.dto.media.MediaRequest;
import org.social.common.validation.MaxWords;

import java.util.List;

public record CommentUpdateRequest(
        @NotBlank @MaxWords(value = 40, message = "Nội dung bình luận không được vượt quá 40 từ") String content,

        @Valid List<MediaRequest> media) {
}
