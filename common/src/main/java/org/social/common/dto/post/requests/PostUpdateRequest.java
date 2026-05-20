package org.social.common.dto.post.requests;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PostUpdateRequest(
        @NotEmpty(message = "Nội dung bài viết không được để trống") List<@NotEmpty(message = "Nội dung không được để trống") String> contents
) {}
