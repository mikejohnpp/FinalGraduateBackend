package org.social.apigateway.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.social.apigateway.services.JWTService;
import org.social.apigateway.services.UserService;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.JwtAuthResponse;
import org.social.common.dto.LoginRequest;
import org.social.common.dto.RegisterRequest;
import org.social.common.entities.User;
import org.social.common.exceptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    /**
     * POST /api/auth/register
     *
     * Body (JSON):
     * {
     * "email": "user@example.com",
     * "matKhau": "123456",
     * "xacNhanMatKhau": "123456"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> dangKy(@Validated @RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResponse.ok("Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
    }

    /**
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> dangNhap(@Validated @RequestBody LoginRequest request,
                                                                 HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(request.getEmail());
            String refreshToken = jwtService.createRefreshToken(request.getEmail());

            User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));

            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
            // cookie.setSecure(true); // Bỏ comment nếu chạy HTTPS
            response.addCookie(cookie);

            return ApiResponse.ok("Đăng nhập thành công!", new JwtAuthResponse(token, user.getId()));
        } else {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng.");
        }
    }

    /**
     * GET /api/auth/active?code={maKichHoat}
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Void>> kichHoat(@RequestParam("code") String maKichHoat) {
        boolean thanhCong = userService.kichHoatTaiKhoan(maKichHoat);

        if (thanhCong) {
            return ApiResponse.ok("Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.");
        } else {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Mã kích hoạt không hợp lệ hoặc đã hết hạn.");
        }
    }

    /**
     * POST /api/auth/refresh-token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "Không tìm thấy refresh token trong cookie.");
        }

        try {
            String email = jwtService.extractEmail(refreshToken);
            org.springframework.security.core.userdetails.UserDetails userDetails = userService.loadUserByUsername(email);

            if (jwtService.validateToken(refreshToken, userDetails)) {
                String newToken = jwtService.generateToken(email);
                String newRefreshToken = jwtService.createRefreshToken(email);

                User user = userService.findByEmail(email)
                        .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));

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
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ hoặc đã hết hạn.");
        }
    }
//    @GetMapping("/test-token")
//    public ResponseEntity<ApiResponse<Void>> test(){
//        return ApiResponse.ok("rất thành công");
//    }

    /**
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> dangXuat(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            Cookie cookie = new Cookie("refreshToken", "");
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0); // Hết hạn ngay lập tức để xóa cookie
            // cookie.setSecure(true); // Bỏ comment nếu chạy HTTPS
            response.addCookie(cookie);
        }

        return ApiResponse.ok("Đăng xuất thành công!");
    }
}
