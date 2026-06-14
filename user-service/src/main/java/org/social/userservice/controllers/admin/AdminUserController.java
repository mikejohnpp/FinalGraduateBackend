package org.social.userservice.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.PageResponse;
import org.social.common.dto.admin.UserAdminDTO;
import org.social.common.dto.admin.requests.AdminUserCreateRequest;
import org.social.common.dto.admin.requests.AdminUserUpdateRequest;
import org.social.userservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserAdminDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        PageResponse<UserAdminDTO> result = userService.getAllUsers(page, size, search);
        return ApiResponse.ok("Lấy danh sách người dùng thành công", result);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserAdminDTO>> createUser(
            @Validated @RequestBody AdminUserCreateRequest request) {
        UserAdminDTO result = userService.createUserAdmin(request);
        return ApiResponse.created("Tạo người dùng thành công", result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAdminDTO>> updateUser(
            @PathVariable long id,
            @Validated @RequestBody AdminUserUpdateRequest request) {
        UserAdminDTO result = userService.updateUserAdmin(id, request);
        return ApiResponse.ok("Cập nhật người dùng thành công", result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable long id) {
        userService.deleteUserAdmin(id);
        return ApiResponse.ok("Xóa người dùng thành công");
    }
}
