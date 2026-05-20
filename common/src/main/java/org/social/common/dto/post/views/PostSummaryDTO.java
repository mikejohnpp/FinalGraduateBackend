package org.social.common.dto.post.views;

import java.time.Instant;

public record PostSummaryDTO(
        Integer id,
        String authorName,
        Boolean isGroupPosted,
        Instant createdAt,
        int commentCount
) {}
