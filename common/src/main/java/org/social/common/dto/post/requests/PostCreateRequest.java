package org.social.common.dto.post.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostCreateRequest(
        @NotNull(message = "ID người dùng không được để trống") Integer userId,
        Boolean isGroupPosted,
        Integer groupId,
        @NotBlank(message = "Nội dung bài viết không được để trống") String content
) {}
