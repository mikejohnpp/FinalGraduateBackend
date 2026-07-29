package org.social.chatservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.social.common.dto.conversation.response.ChatMessageResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.social.chatservice.services.ChatRedisService;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRedisServiceImpl implements ChatRedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_MESSAGES = 200;

    @Override
    public void saveMessage(ChatMessageResponse message) {

        String key = "chat:conversation:" + message.getConversationId();

        try {
            String json = objectMapper.writeValueAsString(message);

            stringRedisTemplate.opsForList().rightPush(key, json);

            stringRedisTemplate.opsForList().trim(key, -MAX_MESSAGES, -1);
            stringRedisTemplate.expire(key, Duration.ofMinutes(1));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không thể lưu message vào Redis", e);
        }
    }

    @Override
    public List<ChatMessageResponse> getMessages(Integer conversationId, int limit) {

        String key = "chat:conversation:" + conversationId;

        List<String> jsons = stringRedisTemplate.opsForList()
                .range(key, -limit, -1);

        if (jsons == null) {
            return List.of();
        }

        return jsons.stream()
                .map(this::fromJson)
                .toList();
    }

    private ChatMessageResponse fromJson(String json) {
        try {
            return objectMapper.readValue(json, ChatMessageResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không thể đọc message từ Redis", e);
        }
    }

}
