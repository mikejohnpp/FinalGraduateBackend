package org.social.common.dto.post.requests;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PostUpdateRequest(
        @NotEmpty List<@NotEmpty String> contents
) {}
