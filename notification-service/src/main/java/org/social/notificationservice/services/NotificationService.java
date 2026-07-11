package org.social.notificationservice.services;

import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.notification.views.NotificationDTO;
import org.social.common.events.NotificationEvent;

public interface NotificationService {

    /** Lưu 1 thông báo từ sự kiện Kafka */
    void createFromEvent(NotificationEvent event);

    /** Danh sách thông báo (tất cả hoặc chỉ chưa đọc), mới nhất trước */
    CursorPageResponse<NotificationDTO> getNotifications(Integer userId, boolean unreadOnly, String cursor, int size);

    /** Số thông báo chưa đọc */
    long countUnread(Integer userId);

    /** Đánh dấu 1 thông báo đã đọc */
    void markAsRead(Long id, Integer userId);

    /** Đánh dấu tất cả thông báo đã đọc */
    void markAllAsRead(Integer userId);
}
