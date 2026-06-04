package org.social.chatservice.controllers;


import lombok.RequiredArgsConstructor;
import org.social.chatservice.services.ConversationService;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.conversation.requests.CreateConversationRequest;
import org.social.common.dto.conversation.response.ConversationResponse;
import org.social.common.dto.conversation.response.ConversationResponseDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;


    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(@RequestBody CreateConversationRequest request){
        return conversationService.createConversation(request.getUserOppenentId(),request.getUserCurrentId());
    }

    @PostMapping("/create_group")
    public ResponseEntity<ApiResponse<ConversationResponse>> createGroupConversation(@RequestBody org.social.common.dto.conversation.requests.CreateConversationGroupRequest request){
        return conversationService.createGroupConversation(request);
    }

    @PostMapping("/{conversationId}/members")
    public ResponseEntity<ApiResponse<ConversationResponse>> addMembersToGroup(
            @PathVariable int conversationId,
            @RequestBody org.social.common.dto.conversation.requests.AddMemberRequest request){
        return conversationService.addMembersToGroup(conversationId, request);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Set<ConversationResponse>>> getConversations(@PathVariable int userId){

        Set<ConversationResponse> conversations = conversationService.getAllConversations(userId);

        return ApiResponse.ok("Lấy ra danh sách conversation của user hiên tại", conversations);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponseDetail>> getConversationDetailById(@PathVariable int conversationId,
                                                                                             @RequestParam(defaultValue = "0") int page,
                                                                                             @RequestParam(defaultValue = "50") int size){
        return ApiResponse.ok(
                "Lấy cuộc trò chuyện thành công",
                conversationService.getConversationDetail(
                        conversationId,
                        page,
                        size
                )
        );
    }
}