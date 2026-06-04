package org.social.chatservice.controllers;


import lombok.RequiredArgsConstructor;
import org.social.chatservice.services.MessageService;
import org.social.common.dto.conversation.requests.ChatMessageRequest;
import org.social.common.dto.conversation.response.ChatMessageResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {


    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;



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
                saved
        );
    }

    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload org.social.common.dto.conversation.requests.TypingIndicator request, Principal principal) {
        request.setSenderId(Integer.valueOf(principal.getName()));
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId() + "/typing",
                request
        );
    }
}
