package org.social.common.dto.comment.requests;

import jakarta.validation.constraints.NotNull;

public record CommentLikeRequest(
        @NotNull
        Integer userId
) {}
