package org.social.chatservice.websocket;

import org.social.chatservice.dto.TokenValidateResponse;
import org.social.chatservice.feignClient.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.List;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    @Autowired
    UserClient client;

    @Autowired
    RedisSessionManager sessionManager;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

//        System.out.println("=================================");
//        System.out.println("COMMAND = " + accessor.getCommand());
//        System.out.println("SESSION = " + accessor.getSessionId());
//        System.out.println("USER    = " + accessor.getUser());
//        System.out.println("HEADERS = " + accessor.toNativeHeaderMap());
//        System.out.println("=================================");

        try {

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {

//                System.out.println(">>> Bắt đầu CONNECT");
//
                handleConnect(accessor);
//
//                System.out.println(">>> CONNECT thành công");
//                System.out.println(">>> USER sau khi set = " + accessor.getUser());

            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {

//                System.out.println(">>> DISCONNECT");
//                System.out.println(">>> USER = " + accessor.getUser());

                sessionManager.removeSession(accessor.getSessionId());
            }

        } catch (Exception e) {

//            System.out.println(">>> LỖI TRONG INTERCEPTOR");
            e.printStackTrace();

            throw e;
        }

        return message;
    }

    public void handleConnect(StompHeaderAccessor accessor) {

//        System.out.println("===== HANDLE CONNECT =====");

        String authorization = accessor.getFirstNativeHeader("Authorization");

//        System.out.println("Authorization = " + authorization);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing token");
        }

//        System.out.println("Gọi user-service validate token...");

        TokenValidateResponse response = client.validateToken(authorization);

//        System.out.println("Validate response = " + response);

        Map<String, String> data = response.getData();

//        System.out.println("Data = " + data);

        int userId = Integer.parseInt(data.get("userId"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                data.get("userId"),
                null,
                data.get("role") != null
                        ? List.of(
                                new SimpleGrantedAuthority(
                                        data.get("role")))
                        : List.of());

//        System.out.println("Authentication = " + authentication);

        accessor.setUser(authentication);

//        System.out.println("Principal sau set = "
//                + accessor.getUser());

        sessionManager.addSession(
                userId,
                accessor.getSessionId());

//        System.out.println("Session đã lưu");
//        System.out.println("userId = " + userId);
//        System.out.println("sessionId = " + accessor.getSessionId());
//
//        System.out.println("===== CONNECT DONE =====");
    }

}
