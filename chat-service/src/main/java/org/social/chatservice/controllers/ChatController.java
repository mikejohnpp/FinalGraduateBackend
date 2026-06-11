package org.social.chatservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.chatservice.services.MessageService;
import org.social.common.dto.conversation.requests.ChatMessageRequest;
import org.social.common.dto.conversation.response.ChatMessageResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.social.common.dto.call.request.CallSignalRequest;
import org.social.common.dto.call.request.CallOfferRequest;
import org.social.common.dto.call.request.CallAnswerRequest;
import org.social.common.dto.call.request.IceCandidateRequest;
import org.social.common.dto.call.response.CallSignalResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest chatMessageRequest, Principal principal) {
        // Gán đúng username của người đang kết nối (tránh giả mạo)
        System.out.println(principal.getName());
        chatMessageRequest.setSenderId(Integer.valueOf(principal.getName()));

        // Lưu vào DB
        ChatMessageResponse saved = messageService.saveMessage(chatMessageRequest);

        // Broadcast tới tất cả thành viên trong conversation
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + saved.getConversationId(),
                saved);
    }

    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload org.social.common.dto.conversation.requests.TypingIndicator request,
            Principal principal) {
        request.setSenderId(Integer.valueOf(principal.getName()));
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId() + "/typing",
                request);
    }

    @MessageMapping("/call.signal")
    public void handleCallSignal(@Payload CallSignalRequest request, Principal principal) {

        Long toUserId = null;

        try {
            switch (request.type()) {
                case "OFFER":
                    toUserId = objectMapper.convertValue(request.payload(), CallOfferRequest.class).toUserId();
                    break;
                case "ANSWER":
                    toUserId = objectMapper.convertValue(request.payload(), CallAnswerRequest.class).toUserId();
                    break;
                case "ICE":
                    toUserId = objectMapper.convertValue(request.payload(), IceCandidateRequest.class).toUserId();
                    break;
                case "REJECT":
                case "HANGUP":
                    Map<?, ?> map = (Map<?, ?>) request.payload();
                    toUserId = Long.valueOf(map.get("toUserId").toString());
                    break;
                default:
                    System.err.println("Unknown call signal type: " + request.type());
                    return;
            }
        } catch (Exception e) {
            System.err.println("Failed to parse payload for type " + request.type() + ": " + e.getMessage());
            return;
        }

        if (toUserId != null) {
            Long fromUserId = Long.valueOf(principal.getName());

            Map<String, Object> enrichedPayload = new HashMap<>();
            if (request.payload() instanceof Map<?, ?> existingMap) {
                existingMap.forEach((k, v) -> enrichedPayload.put(k.toString(), v));
            }
            enrichedPayload.put("fromUserId", fromUserId);

            CallSignalResponse response = new CallSignalResponse(request.type(), enrichedPayload);
            messagingTemplate.convertAndSendToUser(
                    toUserId.toString(),
                    "/queue/call",
                    response);

            System.out.println("Forwarded call signal [" + request.type() + "] from " + fromUserId + " to " + toUserId);
        } else {
            System.err.println("Could not extract toUserId from call signal payload: " + request.payload());
        }
    }
}
