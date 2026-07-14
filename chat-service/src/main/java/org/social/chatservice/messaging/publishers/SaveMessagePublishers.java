package org.social.chatservice.messaging.publishers;


import lombok.RequiredArgsConstructor;
import org.social.common.events.AnalyzeSentimentEvent;
import org.social.common.events.PingEvent;
import org.social.common.events.SaveMessageEvent;
import org.social.common.kafka.config.KafkaCommonProperties;
import org.social.common.kafka.support.EventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveMessagePublishers {

    private final EventPublisher publisher;
    private final KafkaCommonProperties properties;

    public void sendMessage(SaveMessageEvent event) {
        publisher.publish(
                properties.getTopics().getSaveMessage(),
                String.valueOf(event.id()),
                "dev.mess.saved",
                event
        );
    }


}
