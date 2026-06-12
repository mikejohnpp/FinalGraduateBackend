package org.social.chatservice.websocket;


import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private final Map<Integer, Set<String>> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Integer> sessionToUser = new ConcurrentHashMap<>();

    public void addSession(Integer userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionToUser.put(sessionId, userId);
    }

    public void removeSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        Integer userId = sessionToUser.remove(sessionId);
        if (userId == null) {
            return;
        }

        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }
    public Set<Integer> getOnlineUsers() {
        return Set.copyOf(userSessions.keySet());
    }

    public Set<String> getSessions(Long userId) {
        return userSessions.getOrDefault(userId.intValue(), Set.of());
    }
    public boolean isOnline(Long userId) {
        Set<String> sessions = userSessions.get(userId.intValue());
        return sessions != null && !sessions.isEmpty();
    }
}
