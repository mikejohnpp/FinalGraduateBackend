package org.social.common.dto.post.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostCreateRequest(
        @NotNull(message = "ID người dùng không được để trống") Integer userId,
        Boolean isGroupPosted,
        Integer groupId,
        @NotEmpty(message = "Nội dung bài viết không được để trống") List<@NotEmpty(message = "Nội dung không được để trống") String> contents
) {}
