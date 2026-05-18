package org.social.common.dto.post.views;

import java.time.Instant;

public record PostDTO(
        Integer id,
        String authorName,
        Boolean isGroupPosted,
        Instant createdAt
) {}
