package org.social.common.dto.post.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @NotBlank(message = "Nội dung bài viết không được để trống") 
        @Size(max = 40, message = "Nội dung bài viết không được vượt quá 40 ký tự")
        String content
) {}
