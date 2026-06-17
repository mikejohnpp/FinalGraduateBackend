package org.social.common.dto.group.requests;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MemberRequestActionRequest(
        @NotEmpty List<Integer> requestIds
) {}
