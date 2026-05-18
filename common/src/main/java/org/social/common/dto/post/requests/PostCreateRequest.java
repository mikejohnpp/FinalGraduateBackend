package org.social.common.dto.post.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostCreateRequest(
        @NotNull Integer userId,
        Boolean isGroupPosted,
        Integer groupId,
        @NotEmpty List<@NotEmpty String> contents
) {}
