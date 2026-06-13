package org.social.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.search.SearchResultDTO;
import org.social.common.dto.user.views.UserProfileDTO;
import org.social.userservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.social.common.dto.user.request.ProfileUpdateRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResultDTO>> search(@RequestParam(defaultValue = "") String q) {
        SearchResultDTO result = userService.search(q);
        return ApiResponse.ok("Tìm kiếm thành công", result);
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getProfile(
            @PathVariable long id,
            @RequestHeader("X-User-Email") String requestingEmail) {
        UserProfileDTO dto = userService.getUserProfile(id, requestingEmail);
        return ApiResponse.ok("Lấy thông tin người dùng thành công", dto);
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateProfile(
            @RequestParam long userId,
            @Validated @RequestBody ProfileUpdateRequest request) {
        UserProfileDTO dto = userService.updateProfile(userId, request);
        return ApiResponse.ok("Cập nhật thông tin thành công!", dto);
    }

    @PostMapping("/profile/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam long userId,
            @RequestParam("file") MultipartFile file) {
        String url = userService.uploadAvatar(userId, file);
        return ApiResponse.ok("Cập nhật ảnh đại diện thành công!", url);
    }

    @PostMapping("/profile/cover")
    public ResponseEntity<ApiResponse<String>> uploadCover(
            @RequestParam long userId,
            @RequestParam("file") MultipartFile file) {
        String url = userService.uploadCover(userId, file);
        return ApiResponse.ok("Cập nhật ảnh bìa thành công!", url);
    }
}

