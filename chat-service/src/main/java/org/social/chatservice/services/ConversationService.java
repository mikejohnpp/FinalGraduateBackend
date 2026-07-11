package org.social.chatservice.services;

import org.social.common.dto.ApiResponse;
import org.social.common.dto.conversation.requests.CreateConversationGroupRequest;
import org.social.common.dto.conversation.response.ConversationResponse;
import org.social.common.dto.conversation.response.ConversationResponseDetail;
import org.springframework.http.ResponseEntity;

import java.util.Set;


public interface ConversationService {

    Set<ConversationResponse> getAllConversations(int userId);

    ResponseEntity<ApiResponse<ConversationResponse>> createConversation(int userOppenentId,int userCurrentId);

    ResponseEntity<ApiResponse<ConversationResponse>> createGroupConversation(CreateConversationGroupRequest request);

    ResponseEntity<ApiResponse<ConversationResponse>> addMembersToGroup(int conversationId, org.social.common.dto.conversation.requests.AddMemberRequest request);

    ConversationResponseDetail getConversationDetail(int conversationId, int page, int size);

    ConversationResponseDetail getConversationDetailImageAndFile(int conversationId);
}
