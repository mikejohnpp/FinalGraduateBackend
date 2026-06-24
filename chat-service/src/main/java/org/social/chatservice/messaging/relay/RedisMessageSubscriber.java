package org.social.chatservice.messaging.relay;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedisMessageSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public RedisMessageSubscriber(SimpMessagingTemplate messagingTemplate,
            @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void onMessage(String json) {
        try {
            BroadcastMessage message = objectMapper.treeToValue(
                    objectMapper.readTree(json), BroadcastMessage.class);

            // Convert the JSON tree payload into a Map so the broker's Jackson
            // converter serializes it back into a proper JSON object for clients.
            Map<String, Object> payloadMap = message.payload() == null
                    ? Map.of()
                    : objectMapper.convertValue(message.payload(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                            });
            Object payload = payloadMap;

            if (message.sendToUser() != null) {
                messagingTemplate.convertAndSendToUser(
                        message.sendToUser(),
                        message.destination(),
                        payload);
            } else {
                messagingTemplate.convertAndSend(message.destination(), payload);
            }
        } catch (Exception e) {
            System.err.println("Failed to handle Redis broadcast message: " + e.getMessage());
        }
    }
}
