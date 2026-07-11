package org.social.notificationservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.notification.mappers.NotificationMapper;
import org.social.common.dto.notification.views.NotificationDTO;
import org.social.common.entities.Notification;
import org.social.common.entities.NotificationType;
import org.social.common.entities.User;
import org.social.common.events.NotificationEvent;
import org.social.common.repositories.NotificationRepository;
import org.social.common.repositories.UserRepository;
import org.social.notificationservice.services.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createFromEvent(NotificationEvent event) {
        if (event.recipientId() == null || event.type() == null) {
            log.warn("[notification-service] Bỏ qua sự kiện thiếu recipientId hoặc type: {}", event);
            return;
        }

        NotificationType type;
        try {
            type = NotificationType.valueOf(event.type());
        } catch (IllegalArgumentException ex) {
            log.warn("[notification-service] Loại thông báo không hợp lệ: {}", event.type());
            return;
        }

        User recipient = userRepository.findById(Long.valueOf(event.recipientId())).orElse(null);
        if (recipient == null) {
            log.warn("[notification-service] Không tìm thấy người nhận id={}", event.recipientId());
            return;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        if (event.actorId() != null) {
            userRepository.findById(Long.valueOf(event.actorId()))
                    .ifPresent(notification::setActor);
        }
        notification.setType(type);
        notification.setEntityType(event.entityType());
        notification.setEntityId(event.entityId());
        notification.setMessage(event.message());
        notification.setLink(event.link());
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());

        notificationRepository.save(notification);
        log.info("[notification-service] Đã lưu thông báo type={} recipientId={}", type, event.recipientId());
    }

    @Override
    public CursorPageResponse<NotificationDTO> getNotifications(Integer userId, boolean unreadOnly, String cursor,
            int size) {
        Instant cursorInstant = (cursor != null) ? Instant.parse(cursor) : Instant.now();
        PageRequest pageable = PageRequest.of(0, size + 1);

        List<Notification> notifications = unreadOnly
                ? notificationRepository.findUnreadByRecipientBefore(userId, cursorInstant, pageable)
                : notificationRepository.findByRecipientBefore(userId, cursorInstant, pageable);

        boolean hasMore = notifications.size() > size;
        List<Notification> pageData = hasMore ? notifications.subList(0, size) : notifications;

        String nextCursor = pageData.isEmpty() ? null : pageData.getLast().getCreatedAt().toString();

        List<NotificationDTO> dtos = pageData.stream()
                .map(NotificationMapper::toDTO)
                .toList();

        return new CursorPageResponse<>(dtos, nextCursor, hasMore);
    }

    @Override
    public long countUnread(Integer userId) {
        return notificationRepository.countUnread(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Integer userId) {
        notificationRepository.markAsRead(id, userId, Instant.now());
    }

    @Override
    @Transactional
    public void markAllAsRead(Integer userId) {
        notificationRepository.markAllAsRead(userId, Instant.now());
    }
}
