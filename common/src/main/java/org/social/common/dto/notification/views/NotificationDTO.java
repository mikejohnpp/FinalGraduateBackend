package org.social.common.dto.notification.views;

import org.social.common.dto.user.views.AuthorDTO;

import java.time.Instant;

/**
 * DTO trả về cho client hiển thị 1 thông báo.
 */
public record NotificationDTO(
        Long id,
        AuthorDTO actor,
        String type,
        String entityType,
        Integer entityId,
        String message,
        String link,
        Boolean isRead,
        Instant createdAt) {
}
