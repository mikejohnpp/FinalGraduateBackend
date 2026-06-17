package org.social.common.dto.admin.sentiment;

import java.time.Instant;

public record SentimentItemDTO(
        String type,
        Integer id,
        String content,
        String sentiment,
        Double confidence,
        Integer authorId,
        String authorName,
        Integer groupId,
        String groupName,
        Instant createdAt) {
}
