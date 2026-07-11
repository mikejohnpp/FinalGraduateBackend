package org.social.userservice.messaging.publishers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.common.events.NotificationEvent;
import org.social.common.kafka.support.EventEnvelope;
import org.social.common.kafka.support.EventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helper phát sự kiện tạo thông báo lên Kafka. notification-service sẽ tiêu thụ
 * và lưu vào DB. Fire-and-forget: lỗi publish chỉ được log, không làm fail
 * nghiệp vụ chính.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    @Value("${app.kafka.topics.notification.created}")
    private String notificationTopic;

    private final EventPublisher userEventPublisher;

    /**
     * Phát sự kiện thông báo. Bỏ qua nếu recipient trùng actor (không tự thông
     * báo cho chính mình).
     */
    public void publish(NotificationEvent event) {
        if (event.recipientId() == null) {
            return;
        }
        if (event.actorId() != null && event.actorId().equals(event.recipientId())) {
            return;
        }
        try {
            userEventPublisher.publish(
                    notificationTopic,
                    event.recipientId().toString(),
                    EventEnvelope.of("notificationCreated", "user-service", event));
        } catch (Exception e) {
            log.error("[user-service] Failed to publish notification event type={} recipientId={}: {}",
                    event.type(), event.recipientId(), e.getMessage(), e);
        }
    }
}
