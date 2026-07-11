package org.social.notificationservice.messaging.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.common.events.NotificationEvent;
import org.social.common.kafka.support.EventEnvelope;
import org.social.notificationservice.services.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final ObjectMapper kafkaObjectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "${app.kafka.topics.notification.created}", groupId = "notification-service", containerFactory = "kafkaListenerContainerFactory")
    public void onNotificationCreated(EventEnvelope<Object> envelope, Acknowledgment ack) {
        try {
            NotificationEvent payload = kafkaObjectMapper.convertValue(envelope.payload(), NotificationEvent.class);
            notificationService.createFromEvent(payload);
        } catch (Exception e) {
            log.error("[notification-service] Lỗi xử lý sự kiện thông báo (eventId={}): {}",
                    envelope.eventId(), e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
