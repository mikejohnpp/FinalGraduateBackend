package org.social.chatservice.messaging;

import lombok.RequiredArgsConstructor;
import org.social.common.events.PingEvent;
import org.social.common.kafka.config.KafkaCommonProperties;
import org.social.common.kafka.support.EventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PingPublisher {

    private final EventPublisher publisher;
    private final KafkaCommonProperties properties;

    public void sendPing(PingEvent event) {
        publisher.publish(
                properties.getTopics().getDemoPing(),
                event.from(),
                "demo.ping",
                event
        );
    }
}
