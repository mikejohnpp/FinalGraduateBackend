package org.social.chatservice.services;

import org.social.common.dto.ApiResponse;
import org.social.common.dto.conversation.requests.CreateConversationGroupRequest;
import org.social.common.dto.conversation.response.ConversationResponse;
import org.social.common.dto.conversation.response.ConversationResponseDetail;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public interface ConversationService {

    Set<ConversationResponse> getAllConversations(int userId, String userIdRequest);

    ResponseEntity<ApiResponse<ConversationResponse>> createConversation(int userOppenentId, int userCurrentId,String userIdRequest);

    /**
     * Tạo sẵn (hoặc lấy) cuộc trò chuyện 1-1 giữa hai người. Idempotent: nếu đã
     * tồn tại thì không tạo mới. Dùng cho luồng sự kiện (vd. kết bạn thành công).
     */
    void ensurePrivateConversation(int userAId, int userBId);

    ResponseEntity<ApiResponse<ConversationResponse>> createGroupConversation(CreateConversationGroupRequest request,String userIdRequest);

    ResponseEntity<ApiResponse<ConversationResponse>> addMembersToGroup(int conversationId,
            org.social.common.dto.conversation.requests.AddMemberRequest request);

    ConversationResponseDetail getConversationDetail(int conversationId, int page, int size);

    ConversationResponseDetail getConversationDetailImageAndFile(int conversationId);
}
