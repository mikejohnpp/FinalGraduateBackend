package org.social.common.dto.post.requests;

import jakarta.validation.constraints.NotBlank;

public record PostUpdateRequest(
        @NotBlank(message = "Nội dung bài viết không được để trống") String content
) {}
