package org.social.chatservice.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RedisSessionManager {

    private static final String USER_SESSIONS_PREFIX = "chat:online:user:";
    private static final String SESSION_USER_PREFIX = "chat:online:session:";
    private static final Duration SESSION_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    private final Map<String, Integer> localSessions = new ConcurrentHashMap<>();

    public void addSession(Integer userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }
        String userKey = userSessionsKey(userId);
        String sessionKey = sessionUserKey(sessionId);

        redisTemplate.opsForSet().add(userKey, sessionId);
        redisTemplate.opsForValue().set(sessionKey, String.valueOf(userId), SESSION_TTL);
        redisTemplate.expire(userKey, SESSION_TTL);

        localSessions.put(sessionId, userId);
    }

    public void removeSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        localSessions.remove(sessionId);

        String userIdValue = redisTemplate.opsForValue().get(sessionUserKey(sessionId));
        if (userIdValue == null) {
            return;
        }
        redisTemplate.delete(sessionUserKey(sessionId));

        String key = userSessionsKey(Integer.valueOf(userIdValue));
        redisTemplate.opsForSet().remove(key, sessionId);
        Long remaining = redisTemplate.opsForSet().size(key);
        if (remaining == null || remaining == 0) {
            redisTemplate.delete(key);
        }
    }

    public Set<Integer> getOnlineUsers() {
        Set<Integer> users = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(USER_SESSIONS_PREFIX + "*").count(100).build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String userIdPart = key.substring(USER_SESSIONS_PREFIX.length());
                try {
                    users.add(Integer.valueOf(userIdPart));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return users;
    }

    public Set<String> getSessions(Long userId) {
        Set<String> sessions = redisTemplate.opsForSet().members(userSessionsKey(userId.intValue()));
        return sessions != null ? sessions : Set.of();
    }

    public boolean isOnline(Long userId) {
        Boolean exists = redisTemplate.hasKey(userSessionsKey(userId.intValue()));
        return Boolean.TRUE.equals(exists);
    }

    @Scheduled(fixedRate = 20000)
    public void refreshTtls() {
        for (Map.Entry<String, Integer> entry : localSessions.entrySet()) {
            String sessionId = entry.getKey();
            Integer userId = entry.getValue();
            redisTemplate.expire(sessionUserKey(sessionId), SESSION_TTL);
            redisTemplate.expire(userSessionsKey(userId), SESSION_TTL);
        }
    }

    private String userSessionsKey(Integer userId) {
        return USER_SESSIONS_PREFIX + userId;
    }

    private String sessionUserKey(String sessionId) {
        return SESSION_USER_PREFIX + sessionId;
    }
}
