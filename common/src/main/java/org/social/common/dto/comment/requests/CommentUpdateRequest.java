package org.social.common.dto.comment.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.social.common.dto.media.MediaRequest;

import java.util.List;

public record CommentUpdateRequest(
                @NotBlank @Size(max = 40, message = "Nội dung bình luận không được vượt quá 40 ký tự") String content,

                @Valid List<MediaRequest> media) {
}
