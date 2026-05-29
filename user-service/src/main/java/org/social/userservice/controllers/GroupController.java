package org.social.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.group.requests.GroupCreateRequest;
import org.social.common.dto.group.views.GroupDTO;
import org.social.common.dto.group.views.GroupMemberDTO;
import org.social.common.dto.post.views.PostSummaryDTO;
import org.social.userservice.services.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupDTO>> create(
            @Validated @RequestBody GroupCreateRequest request,
            @RequestParam Integer userId) {
        GroupDTO dto = groupService.create(request, userId);
        return ApiResponse.created("Tạo nhóm thành công!", dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupDTO>> getById(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        GroupDTO dto = groupService.getById(id, userId);
        return ApiResponse.ok("Lấy chi tiết nhóm thành công!", dto);
    }

    @GetMapping("/joined")
    public ResponseEntity<ApiResponse<List<GroupDTO>>> getJoined(@RequestParam Integer userId) {
        List<GroupDTO> list = groupService.getJoinedGroups(userId);
        return ApiResponse.ok("Lấy danh sách nhóm đã tham gia thành công!", list);
    }

    @GetMapping("/suggested")
    public ResponseEntity<ApiResponse<List<GroupDTO>>> getSuggested(@RequestParam Integer userId) {
        List<GroupDTO> list = groupService.getSuggestedGroups(userId);
        return ApiResponse.ok("Lấy danh sách nhóm gợi ý thành công!", list);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<Void>> join(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        groupService.join(id, userId);
        return ApiResponse.ok("Tham gia nhóm thành công!");
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<ApiResponse<Void>> leave(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        groupService.leave(id, userId);
        return ApiResponse.ok("Đã rời khỏi nhóm!");
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberDTO>>> getMembers(@PathVariable Integer id) {
        List<GroupMemberDTO> list = groupService.getMembers(id);
        return ApiResponse.ok("Lấy danh sách thành viên thành công!", list);
    }

    @GetMapping("/posts/feed")
    public ResponseEntity<ApiResponse<CursorPageResponse<PostSummaryDTO>>> getFeed(
            @RequestParam Integer userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size) {
        CursorPageResponse<PostSummaryDTO> result = groupService.getGroupFeed(userId, cursor, size);
        return ApiResponse.ok("Lấy bảng tin nhóm thành công!", result);
    }
    @GetMapping("/{id}/posts")
    public ResponseEntity<ApiResponse<CursorPageResponse<PostSummaryDTO>>> getGroupPosts(
            @PathVariable Integer id,
            @RequestParam Integer userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size) {
        CursorPageResponse<PostSummaryDTO> result = groupService.getGroupPosts(id, userId, cursor, size);
        return ApiResponse.ok("Lấy bài viết của nhóm thành công!", result);
    }
}
