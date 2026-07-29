package org.social.chatservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.social.chatservice.services.ConversationMemberCache;
import org.social.common.repositories.ConversationUserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationMemberCacheImpl implements ConversationMemberCache {

    private static final String KEY_PREFIX = "conversation:members:";
    private static final Duration TTL = Duration.ofHours(6);

    private final StringRedisTemplate redisTemplate;
    private final ConversationUserRepository conversationUserRepository;

    @Override
    public List<Integer> getMemberIds(Integer conversationId) {
        String key = key(conversationId);

        try {
            Set<String> cached = redisTemplate.opsForSet().members(key);
            if (cached != null && !cached.isEmpty()) {
                return cached.stream().map(Integer::valueOf).toList();
            }
        } catch (Exception e) {
            log.warn("[chat-service] Đọc cache thành viên hội thoại {} lỗi, fallback DB: {}",
                    conversationId, e.getMessage());
            return conversationUserRepository.findUserIdsByConversationId(conversationId);
        }

        List<Integer> memberIds = conversationUserRepository.findUserIdsByConversationId(conversationId);
        if (memberIds.isEmpty()) {
            return memberIds;
        }

        try {
            String[] values = memberIds.stream().map(String::valueOf).toArray(String[]::new);
            redisTemplate.opsForSet().add(key, values);
            redisTemplate.expire(key, TTL);
        } catch (Exception e) {
            log.warn("[chat-service] Nạp cache thành viên hội thoại {} lỗi: {}",
                    conversationId, e.getMessage());
        }

        return memberIds;
    }

    @Override
    public void evict(Integer conversationId) {
        try {
            redisTemplate.delete(key(conversationId));
        } catch (Exception e) {
            log.warn("[chat-service] Xóa cache thành viên hội thoại {} lỗi: {}",
                    conversationId, e.getMessage());
        }
    }

    private String key(Integer conversationId) {
        return KEY_PREFIX + conversationId;
    }
}
