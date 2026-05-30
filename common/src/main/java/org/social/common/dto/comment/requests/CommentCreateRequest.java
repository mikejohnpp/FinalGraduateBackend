package org.social.common.dto.comment.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotNull
        Integer userId,

        @NotBlank
        @Size(max = 2000)
        String content,

        Integer parentId
) {}
