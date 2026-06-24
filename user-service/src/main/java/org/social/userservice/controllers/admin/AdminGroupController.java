package org.social.userservice.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.PageResponse;
import org.social.common.dto.admin.GroupAdminDTO;
import org.social.common.dto.admin.requests.AdminGroupUpdateRequest;
import org.social.userservice.services.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/groups")
@RequiredArgsConstructor
public class AdminGroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GroupAdminDTO>>> getAllGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        PageResponse<GroupAdminDTO> result = groupService.getAllGroups(page, size, search);
        return ApiResponse.ok("Lấy danh sách nhóm thành công", result);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupAdminDTO>> createGroup(
            @Validated @RequestBody org.social.common.dto.admin.requests.AdminGroupCreateRequest request) {
        GroupAdminDTO result = groupService.createGroupAdmin(request);
        return ApiResponse.created("Tạo nhóm thành công", result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupAdminDTO>> updateGroup(
            @PathVariable Integer id,
            @Validated @RequestBody AdminGroupUpdateRequest request) {
        GroupAdminDTO result = groupService.updateGroupAdmin(id, request);
        return ApiResponse.ok("Cập nhật nhóm thành công", result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Integer id) {
        groupService.deleteGroupAdmin(id);
        return ApiResponse.ok("Xóa nhóm thành công");
    }
}
