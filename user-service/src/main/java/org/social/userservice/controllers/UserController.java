package org.social.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.user.views.UserProfileDTO;
import org.social.userservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getProfile(
            @PathVariable long id,
            @RequestHeader("X-User-Email") String requestingEmail) {
        UserProfileDTO dto = userService.getUserProfile(id, requestingEmail);
        return ApiResponse.ok("Lấy thông tin người dùng thành công", dto);
    }
}
