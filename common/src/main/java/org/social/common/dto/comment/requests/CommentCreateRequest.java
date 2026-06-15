package org.social.common.dto.comment.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotNull
        Integer userId,

        @NotBlank
        @Size(max = 40, message = "Nội dung bình luận không được vượt quá 40 ký tự")
        String content,

        Integer parentId
) {}
