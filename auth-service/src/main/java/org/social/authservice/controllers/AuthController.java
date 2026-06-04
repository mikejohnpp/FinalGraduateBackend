package org.social.authservice.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.social.authservice.services.JWTService;
import org.social.authservice.services.UserService;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.JwtAuthResponse;
import org.social.common.entities.User;
import org.social.common.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "Không tìm thấy refresh token trong cookie.");
        }

        String email = jwtService.extractEmail(refreshToken);
        org.springframework.security.core.userdetails.UserDetails userDetails = userService.loadUserByUsername(email);

        if (jwtService.validateToken(refreshToken, userDetails)) {
            String newToken = jwtService.generateToken(email);
            String newRefreshToken = jwtService.createRefreshToken(email);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User", email));

            Cookie cookie = new Cookie("refreshToken", newRefreshToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
            // cookie.setSecure(true); // Bỏ comment nếu chạy HTTPS
            response.addCookie(cookie);

            return ApiResponse.ok("Làm mới token thành công!", new JwtAuthResponse(newToken, user.getId()));
        } else {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ hoặc đã hết hạn.");
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Object>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        System.out.println("========== VALIDATE TOKEN ==========");
        System.out.println("AUTH HEADER = " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error(
                    HttpStatus.UNAUTHORIZED,
                    "Không tìm thấy token hoặc định dạng sai."
            );
        }

        String token = authHeader.substring(7);

        try {

            Claims claims = jwtService.extractClaim(token, c -> c);

            System.out.println("ALL CLAIMS = " + claims);

            Object userIdObj = claims.get("userId");
            Object roleObj = claims.get("roles");

            System.out.println("userId claim = " + userIdObj);
            System.out.println("userId type = " +
                    (userIdObj != null ? userIdObj.getClass().getName() : "null"));

            System.out.println("roles claim = " + roleObj);

            String email = jwtService.extractEmail(token);

            UserDetails userDetails = userService.loadUserByUsername(email);

            if (!jwtService.validateToken(token, userDetails)) {
                return ApiResponse.error(
                        HttpStatus.UNAUTHORIZED,
                        "Token không khớp với User."
                );
            }

            String role = jwtService.extractClaim(
                    token,
                    c -> c.get("roles", String.class)
            );

            Number userIdNumber = jwtService.extractClaim(
                    token,
                    c -> c.get("userId", Number.class)
            );

            System.out.println("Extracted userIdNumber = " + userIdNumber);

            if (userIdNumber == null) {
                return ApiResponse.error(
                        HttpStatus.UNAUTHORIZED,
                        "Token không chứa userId"
                );
            }

            int userId = userIdNumber.intValue();

            Map<String, String> userData = Map.of(
                    "email", email,
                    "role", role != null ? role : "",
                    "userId", String.valueOf(userId)
            );

            return ApiResponse.ok("Token hợp lệ.", userData);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
