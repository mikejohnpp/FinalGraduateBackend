package org.social.common.dto.post.views;

import java.time.Instant;
import java.util.List;

public record PostDetailDTO(
        Integer id,
        String authorName,
        Boolean isGroupPosted,
        Instant createdAt,
        List<String> contents
) {}
