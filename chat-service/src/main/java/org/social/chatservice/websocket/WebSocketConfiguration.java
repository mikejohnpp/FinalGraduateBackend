package org.social.chatservice.websocket;


import lombok.RequiredArgsConstructor;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // /portfolio is the HTTP URL for the endpoint to which a WebSocket (or SockJS)
        // client needs to connect for the WebSocket handshake
        registry.addEndpoint("/app_socket")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // STOMP messages whose destination header begins with /app are routed to
        // @MessageMapping methods in @Controller classes
        config.setApplicationDestinationPrefixes("/app");
        // Use the built-in message broker for subscriptions and broadcasting and
        // route messages whose destination header begins with /topic or /queue to the broker
        config.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        System.out.println("REGISTER INTERCEPTOR");
        registration.interceptors(webSocketAuthInterceptor);
//        registration.interceptors(new ChannelInterceptor() {
//
//            @Override
//            public Message<?> preSend(Message<?> message, MessageChannel channel) {
//
//                StompHeaderAccessor accessor =
//                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//
//                    System.out.println("da vao connect");
//                    String authHeader = accessor.getFirstNativeHeader("Authorization");
//                    System.out.println(authHeader);
//                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
//
//                        String jwt = authHeader.substring(7);
//                        String email = jwtService.extractEmail(jwt);
//                        List<GrantedAuthority> roles = jwtService.extractRoles(jwt);
//                        UserDetails user =new User(email, "", roles); // User này là của sping Security
//                        System.out.println(user);
//                        if (jwtService.validateToken(jwt, user)) {
//
//                            Authentication auth =
//                                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
//                            System.out.println(auth);
//                            accessor.setUser(auth);
//                        }
//                    }
//                }
//
//                return message;
//            }
//        });
    }

}
