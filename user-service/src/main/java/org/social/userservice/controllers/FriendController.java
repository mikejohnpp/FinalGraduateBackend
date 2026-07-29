package org.social.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.friend.requests.FriendRequest;
import org.social.common.dto.friend.views.FriendRequestDTO;
import org.social.common.dto.friend.views.FriendSuggestionDTO;
import org.social.common.dto.friend.views.FriendshipDTO;
import org.social.userservice.services.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.social.common.dto.friend.views.FriendStatusDTO;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<FriendStatusDTO>> getFriendStatus(
            @RequestParam Integer userId,
            @RequestParam Integer targetId) {
        FriendStatusDTO result = friendService.getFriendStatus(userId, targetId);
        return ApiResponse.ok("Lấy trạng thái bạn bè thành công!", result);
    }

    @DeleteMapping("/requests/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRequest(
            @RequestParam Integer userId,
            @RequestParam Integer targetId) {
        friendService.cancelRequest(userId, targetId);
        return ApiResponse.ok("Đã hủy lời mời kết bạn!");
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<CursorPageResponse<FriendRequestDTO>>> getPendingRequests(
            @RequestParam Integer userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size) {
        CursorPageResponse<FriendRequestDTO> result = friendService.getPendingRequests(userId, cursor, size);
        return ApiResponse.ok("Lấy danh sách lời mời thành công!", result);
    }

    @GetMapping("/requests/count")
    public ResponseEntity<ApiResponse<Integer>> getPendingRequestCount(
            @RequestParam Integer userId) {
        int count = friendService.getPendingRequestCount(userId);
        return ApiResponse.ok("Lấy số lượng lời mời thành công!", count);
    }

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<Void>> sendRequest(
            @Validated @RequestBody FriendRequest request) {
        friendService.sendRequest(request.userId(), request.targetUserId());
        return ApiResponse.created("Đã gửi lời mời kết bạn!");
    }

    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptRequest(
            @PathVariable Integer requestId,
            @RequestParam Integer userId) {
        friendService.acceptRequest(requestId, userId);
        return ApiResponse.ok("Chấp nhận lời mời thành công!");
    }

    @PutMapping("/requests/{requestId}/decline")
    public ResponseEntity<ApiResponse<Void>> declineRequest(
            @PathVariable Integer requestId,
            @RequestParam Integer userId) {
        friendService.declineRequest(requestId, userId);
        return ApiResponse.ok("Đã từ chối lời mời kết bạn!");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<FriendshipDTO>>> getFriends(
            @RequestParam Integer userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        CursorPageResponse<FriendshipDTO> result = friendService.getFriends(userId, cursor, size);
        return ApiResponse.ok("Lấy danh sách bạn bè thành công!", result);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<CursorPageResponse<FriendSuggestionDTO>>> getSuggestions(
            @RequestParam Integer userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        CursorPageResponse<FriendSuggestionDTO> result = friendService.getSuggestions(userId, cursor, size);
        return ApiResponse.ok("Lấy gợi ý bạn bè thành công!", result);
    }

    @DeleteMapping("/{friendUserId}")
    public ResponseEntity<ApiResponse<Void>> unfriend(
            @PathVariable Integer friendUserId,
            @RequestParam Integer userId) {
        friendService.unfriend(userId, friendUserId);
        return ApiResponse.deleted("Đã huỷ kết bạn thành công!");
    }
}
