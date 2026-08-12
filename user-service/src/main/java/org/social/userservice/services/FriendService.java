package org.social.userservice.services;

import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.friend.views.FriendRequestDTO;
import org.social.common.dto.friend.views.FriendSuggestionDTO;
import org.social.common.dto.friend.views.FriendshipDTO;

import org.social.common.dto.friend.views.FriendStatusDTO;

public interface FriendService {
    CursorPageResponse<FriendRequestDTO> getPendingRequests(Integer userId, String cursor, int size);
    void sendRequest(Integer userId, Integer targetUserId);
    void acceptRequest(Integer requestId, Integer userId);
    void declineRequest(Integer requestId, Integer userId);
    CursorPageResponse<FriendshipDTO> getFriends(Integer userId, String cursor, int size);
    CursorPageResponse<FriendSuggestionDTO> getSuggestions(Integer userId, String cursor, int size);
    void unfriend(Integer userId, Integer friendUserId);
    int getPendingRequestCount(Integer userId);
    FriendStatusDTO getFriendStatus(Integer userId, Integer targetId);
    void cancelRequest(Integer userId, Integer targetId);
    void dismissSuggestion(Integer userId, Integer targetUserId);
}
