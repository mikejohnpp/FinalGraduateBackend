package org.social.common.dto.group.views;

import java.time.Instant;

public record MemberRequestDTO(
        Integer id,
        Integer userId,
        String username,
        String avatarUrl,
        Instant requestedAt,
        String gender,
        Instant joinedPlatformAt
) {}
