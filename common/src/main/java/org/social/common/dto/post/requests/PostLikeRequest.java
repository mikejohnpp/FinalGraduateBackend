package org.social.common.dto.post.requests;

import jakarta.validation.constraints.NotNull;

public record PostLikeRequest(
        @NotNull(message = "ID người dùng không được để trống") Integer userId
) {}
