package org.social.common.dto.comment.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.social.common.dto.media.MediaRequest;

import java.util.List;

public record CommentCreateRequest(
                @NotNull Integer userId,

                @NotBlank @Size(max = 40, message = "Nội dung bình luận không được vượt quá 40 ký tự") String content,

                Integer parentId,

                @Valid List<MediaRequest> media) {
}
