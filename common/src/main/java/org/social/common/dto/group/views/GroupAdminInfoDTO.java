package org.social.common.dto.group.views;

import java.time.Instant;

public record GroupAdminInfoDTO(
        Integer id,
        String name,
        String avatarUrl,
        String coverUrl,
        String privacy,
        long memberCount,
        String description,
        Instant createdAt,
        String role
) {}
