package org.social.authservice.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.social.authservice.services.JWTService;
import org.social.authservice.services.UserService;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.JwtAuthResponse;
import org.social.common.dto.LoginRequest;
import org.social.common.dto.RegisterRequest;
import org.social.common.entities.User;
import org.social.common.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;


//    @PostMapping("/refresh-token")
//    public ResponseEntity<ApiResponse<JwtAuthResponse>> refreshToken(
//            @CookieValue(name = "refreshToken", required = false) String refreshToken,
//            HttpServletResponse response) {
//
//        if (refreshToken == null || refreshToken.trim().isEmpty()) {
//            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "Không tìm thấy refresh token trong cookie.");
//        }
//
//        String email = jwtService.extractEmail(refreshToken);
//        org.springframework.security.core.userdetails.UserDetails userDetails = userService.loadUserByUsername(email);
//
//        if (jwtService.validateToken(refreshToken, userDetails)) {
//            String newToken = jwtService.generateToken(email);
//            String newRefreshToken = jwtService.createRefreshToken(email);
//
//            User user = userService.findByEmail(email)
//                    .orElseThrow(() -> new ResourceNotFoundException("User", email));
//
//            Cookie cookie = new Cookie("refreshToken", newRefreshToken);
//            cookie.setHttpOnly(true);
//            cookie.setPath("/");
//            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
//            // cookie.setSecure(true); // Bỏ comment nếu chạy HTTPS
//            response.addCookie(cookie);
//
//            return ApiResponse.ok("Làm mới token thành công!", new JwtAuthResponse(newToken, user.getId()));
//        } else {
//            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ hoặc đã hết hạn.");
//        }
//    }

    @PostMapping("validate-token")
    public String validateToken() {
        return "validated";
    }
}
