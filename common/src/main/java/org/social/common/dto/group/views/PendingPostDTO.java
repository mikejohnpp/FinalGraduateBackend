package org.social.common.dto.group.views;

import java.time.Instant;

public record PendingPostDTO(
        Integer id,
        Integer authorId,
        String authorName,
        String authorAvatarUrl,
        String content,
        Instant createdAt,
        String status
) {}
