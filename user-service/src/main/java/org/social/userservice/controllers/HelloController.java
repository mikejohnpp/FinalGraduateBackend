package org.social.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.user.views.UserDTO;
import org.social.common.dto.user.views.UserWithAuthenticateDTO;
import org.social.userservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hello")
public class HelloController {
    private final UserService userService;
    @GetMapping
    public List<UserDTO> getAll() {
        return userService.getAll();
    }

    @GetMapping("test")
    public ResponseEntity<ApiResponse<List<UserWithAuthenticateDTO>>> getUserListAuthen() {
        var userAuthen = userService.getListUserWithAuthenticate();
        return ApiResponse.ok("Success", userAuthen);
    }
}
