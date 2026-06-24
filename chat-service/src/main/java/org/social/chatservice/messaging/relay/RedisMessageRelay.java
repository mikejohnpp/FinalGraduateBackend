package org.social.chatservice.messaging.relay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.social.chatservice.config.RedisConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisMessageRelay {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisMessageRelay(StringRedisTemplate redisTemplate,
            @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void broadcast(String destination, Object payload) {
        publish(destination, null, payload);
    }

    public void sendToUser(String userId, String destination, Object payload) {
        publish(destination, userId, payload);
    }

    private void publish(String destination, String userId, Object payload) {
        try {
            JsonNode payloadNode = objectMapper.valueToTree(payload);
            BroadcastMessage message = new BroadcastMessage(destination, userId, payloadNode);
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(RedisConfig.BROADCAST_CHANNEL, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish broadcast message to Redis", e);
        }
    }
}
