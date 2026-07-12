package org.social.chatservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.chatservice.services.ConversationService;
import org.social.chatservice.websocket.RedisSessionManager;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.conversation.requests.CreateConversationRequest;
import org.social.common.dto.conversation.response.ConversationResponse;
import org.social.common.dto.conversation.response.ConversationResponseDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final RedisSessionManager sessionManager;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @RequestBody CreateConversationRequest request,@RequestHeader("X-User-Id") String requestingUserId) {
        return conversationService.createConversation(request.getUserOppenentId(), request.getUserCurrentId(),requestingUserId);
    }

    @PostMapping("/create_group")
    public ResponseEntity<ApiResponse<ConversationResponse>> createGroupConversation(
            @RequestBody org.social.common.dto.conversation.requests.CreateConversationGroupRequest request,@RequestHeader("X-User-Id") String requestingUserId) {
        return conversationService.createGroupConversation(request,requestingUserId);
    }

    @PostMapping("/{conversationId}/members")
    public ResponseEntity<ApiResponse<ConversationResponse>> addMembersToGroup(
            @PathVariable int conversationId,
            @RequestBody org.social.common.dto.conversation.requests.AddMemberRequest request) {
        return conversationService.addMembersToGroup(conversationId, request);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Set<ConversationResponse>>> getConversations(@PathVariable int userId, @RequestHeader("X-User-Id") String requestingUserId) {
        Set<ConversationResponse> conversations = conversationService.getAllConversations(userId,requestingUserId);

        return ApiResponse.ok("Lấy ra danh sách conversation của user hiên tại", conversations);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponseDetail>> getConversationDetailById(
            @PathVariable int conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.ok(
                "Lấy cuộc trò chuyện thành công",
                conversationService.getConversationDetail(
                        conversationId,
                        page,
                        size));
    }

    @GetMapping("/conversationImageAndFile/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponseDetail>> getConversationDetailImageAndFileById(@PathVariable int conversationId){
        return ApiResponse.ok("Truy vấn thành công ",conversationService.getConversationDetailImageAndFile(conversationId));
    }


    @GetMapping("/online")
    public ResponseEntity<ApiResponse<List<Integer>>> getUserOnline() {

        List<Integer> onlineUsers = new ArrayList<>(
                sessionManager.getOnlineUsers());
        System.out.println("user online nef mayas bes" + onlineUsers);
        return ApiResponse.ok("Lấy được danh sách userOnline", onlineUsers);

    }
}