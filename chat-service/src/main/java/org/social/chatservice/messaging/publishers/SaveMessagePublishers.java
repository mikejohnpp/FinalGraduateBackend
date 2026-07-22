package org.social.chatservice.messaging.publishers;


import lombok.RequiredArgsConstructor;
import org.social.common.events.SaveMessageEvent;
import org.social.common.kafka.support.EventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveMessagePublishers {

    private final EventPublisher publisher;

    @Value("${app.kafka.topics.chat.save")
    private String saveMessagesTopic;

    public void sendMessage(SaveMessageEvent event) {
        publisher.publish(
                saveMessagesTopic,
                String.valueOf(event.id()),
                "dev.mess.saved",
                event
        );
    }


}
