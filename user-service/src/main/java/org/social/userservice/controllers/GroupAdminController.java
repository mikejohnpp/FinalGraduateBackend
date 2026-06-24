package org.social.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.PageResponse;
import org.social.common.dto.group.requests.MemberRequestActionRequest;
import org.social.common.dto.group.views.*;
import org.social.userservice.services.GroupAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups/{groupId}/admin")
@RequiredArgsConstructor
public class GroupAdminController {

    private final GroupAdminService groupAdminService;

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<GroupAdminInfoDTO>> getGroupInfo(
            @PathVariable Integer groupId,
            @RequestParam Integer userId) {
        GroupAdminInfoDTO dto = groupAdminService.getGroupInfo(groupId, userId);
        return ApiResponse.ok("Lấy thông tin nhóm thành công!", dto);
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<GroupStatsDTO>> getGroupStats(
            @PathVariable Integer groupId,
            @RequestParam Integer userId) {
        GroupStatsDTO dto = groupAdminService.getGroupStats(groupId, userId);
        return ApiResponse.ok("Lấy thống kê nhóm thành công!", dto);
    }

    @GetMapping("/member-requests")
    public ResponseEntity<ApiResponse<PageResponse<MemberRequestDTO>>> getMemberRequests(
            @PathVariable Integer groupId,
            @RequestParam Integer userId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<MemberRequestDTO> result = groupAdminService.getMemberRequests(groupId, userId, search, gender, sort, page, size);
        return ApiResponse.ok("Lấy danh sách yêu cầu tham gia thành công!", result);
    }

    @PostMapping("/member-requests/approve")
    public ResponseEntity<ApiResponse<Void>> approveMemberRequests(
            @PathVariable Integer groupId,
            @RequestParam Integer userId,
            @Validated @RequestBody MemberRequestActionRequest request) {
        groupAdminService.approveMemberRequests(groupId, userId, request.requestIds());
        return ApiResponse.ok("Đã phê duyệt " + request.requestIds().size() + " thành viên");
    }

    @PostMapping("/member-requests/reject")
    public ResponseEntity<ApiResponse<Void>> rejectMemberRequests(
            @PathVariable Integer groupId,
            @RequestParam Integer userId,
            @Validated @RequestBody MemberRequestActionRequest request) {
        groupAdminService.rejectMemberRequests(groupId, userId, request.requestIds());
        return ApiResponse.ok("Đã từ chối " + request.requestIds().size() + " yêu cầu");
    }

    @GetMapping("/pending-posts")
    public ResponseEntity<ApiResponse<PageResponse<PendingPostDTO>>> getPendingPosts(
            @PathVariable Integer groupId,
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<PendingPostDTO> result = groupAdminService.getPendingPosts(groupId, userId, page, size);
        return ApiResponse.ok("Lấy danh sách bài viết chờ duyệt thành công!", result);
    }

    @PostMapping("/pending-posts/{postId}/approve")
    public ResponseEntity<ApiResponse<Void>> approvePost(
            @PathVariable Integer groupId,
            @PathVariable Integer postId,
            @RequestParam Integer userId) {
        groupAdminService.approvePost(groupId, userId, postId);
        return ApiResponse.ok("Đã phê duyệt bài viết");
    }

    @PostMapping("/pending-posts/{postId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectPost(
            @PathVariable Integer groupId,
            @PathVariable Integer postId,
            @RequestParam Integer userId) {
        groupAdminService.rejectPost(groupId, userId, postId);
        return ApiResponse.ok("Đã từ chối bài viết");
    }
}
