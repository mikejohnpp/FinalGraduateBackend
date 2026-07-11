package org.social.common.dto.notification.mappers;

import org.social.common.dto.notification.views.NotificationDTO;
import org.social.common.dto.user.views.AuthorDTO;
import org.social.common.entities.Notification;
import org.social.common.entities.User;

public class NotificationMapper {

    private static AuthorDTO toAuthorDTO(User user) {
        if (user == null)
            return null;
        return new AuthorDTO(
                user.getId(),
                user.getUserName(),
                user.getAvatar(),
                user.getNickName());
    }

    public static NotificationDTO toDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                toAuthorDTO(notification.getActor()),
                notification.getType() != null ? notification.getType().name() : null,
                notification.getEntityType(),
                notification.getEntityId(),
                notification.getMessage(),
                notification.getLink(),
                notification.getIsRead(),
                notification.getCreatedAt());
    }
}
