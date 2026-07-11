package org.social.common.events;

/**
 * Sự kiện tạo thông báo. Được publish bởi các service nghiệp vụ (user-service)
 * và tiêu thụ bởi notification-service để lưu vào DB.
 *
 * @param recipientId người nhận thông báo
 * @param actorId     người tạo hành động (có thể null với thông báo hệ thống)
 * @param type        loại thông báo (khớp với enum NotificationType)
 * @param entityType  loại thực thể liên quan: POST, COMMENT, FRIEND, GROUP...
 * @param entityId    id thực thể liên quan
 * @param message     nội dung hiển thị sẵn (tuỳ chọn)
 * @param link        đường dẫn điều hướng khi bấm vào thông báo
 */
public record NotificationEvent(
        Integer recipientId,
        Integer actorId,
        String type,
        String entityType,
        Integer entityId,
        String message,
        String link) {
}
