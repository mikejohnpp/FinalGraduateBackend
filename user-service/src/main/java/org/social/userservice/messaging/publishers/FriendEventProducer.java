package org.social.userservice.messaging.publishers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.common.events.FriendAcceptedEvent;
import org.social.common.kafka.support.EventEnvelope;
import org.social.common.kafka.support.EventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helper phát sự kiện kết bạn thành công lên Kafka. chat-service sẽ tiêu thụ và
 * tạo sẵn cuộc trò chuyện 1-1 giữa hai người. Fire-and-forget: lỗi publish chỉ
 * được log, không làm fail nghiệp vụ chính (chấp nhận kết bạn).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FriendEventProducer {

    @Value("${app.kafka.topics.friend.accepted}")
    private String friendAcceptedTopic;

    private final EventPublisher userEventPublisher;

    public void publishAccepted(FriendAcceptedEvent event) {
        try {
            userEventPublisher.publish(
                    friendAcceptedTopic,
                    event.userAId() + ":" + event.userBId(),
                    EventEnvelope.of("friendAccepted", "user-service", event));
        } catch (Exception e) {
            log.error("[user-service] Failed to publish friendAccepted event userA={} userB={}: {}",
                    event.userAId(), event.userBId(), e.getMessage(), e);
        }
    }
}
