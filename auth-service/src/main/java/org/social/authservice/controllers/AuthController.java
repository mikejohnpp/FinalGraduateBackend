package org.social.authservice.controllers;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.social.authservice.services.JWTService;
import org.social.authservice.services.UserService;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.ForgotPasswordRequest;
import org.social.common.dto.JwtAuthResponse;
import org.social.common.dto.LoginRequest;
import org.social.common.dto.RegisterRequest;
import org.social.common.dto.ResetPasswordRequest;
import org.social.common.dto.VerifyOtpRequest;
import org.social.common.entities.User;
import org.social.common.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> dangKy(@Validated @RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResponse.ok("Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> dangNhap(@Validated @RequestBody LoginRequest request,
            HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(request.getEmail());
            String refreshToken = jwtService.createRefreshToken(request.getEmail());

            User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getEmail()));

            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);

            return ApiResponse.ok("Đăng nhập thành công!", new JwtAuthResponse(token, user.getId()));
        } else {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng.");
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Void>> kichHoat(@RequestParam("code") String maKichHoat) {
        boolean thanhCong = userService.kichHoatTaiKhoan(maKichHoat);

        if (thanhCong) {
            return ApiResponse.ok("Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.");
        } else {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Mã kích hoạt không hợp lệ hoặc đã hết hạn.");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "Không tìm thấy refresh token trong cookie.");
        }

        String email = jwtService.extractEmail(refreshToken);
        UserDetails userDetails = userService.loadUserByUsername(email);

        if (jwtService.validateToken(refreshToken, userDetails)) {
            String newToken = jwtService.generateToken(email);
            String newRefreshToken = jwtService.createRefreshToken(email);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User", email));

            Cookie cookie = new Cookie("refreshToken", newRefreshToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);

            return ApiResponse.ok("Làm mới token thành công!", new JwtAuthResponse(newToken, user.getId()));
        } else {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ hoặc đã hết hạn.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> dangXuat(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            Cookie cookie = new Cookie("refreshToken", "");
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }

        return ApiResponse.ok("Đăng xuất thành công!");
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Object>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error(
                    HttpStatus.UNAUTHORIZED,
                    "Không tìm thấy token hoặc định dạng sai.");
        }

        String token = authHeader.substring(7);

        Claims claims = jwtService.extractClaim(token, c -> c);

        String email = jwtService.extractEmail(token);

        UserDetails userDetails = userService.loadUserByUsername(email);

        if (!jwtService.validateToken(token, userDetails)) {
            return ApiResponse.error(
                    HttpStatus.UNAUTHORIZED,
                    "Token không khớp với User.");
        }

        String role = claims.get("roles", String.class);
        Number userIdNumber = claims.get("userId", Number.class);

        if (userIdNumber == null) {
            return ApiResponse.error(
                    HttpStatus.UNAUTHORIZED,
                    "Token không chứa userId");
        }

        int userId = userIdNumber.intValue();

        Map<String, String> userData = Map.of(
                "email", email,
                "role", role != null ? role : "",
                "userId", String.valueOf(userId));

        return ApiResponse.ok("Token hợp lệ.", userData);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> quenMatKhau(@Validated @RequestBody ForgotPasswordRequest request) {
        userService.quenMatKhau(request.getEmail());
        return ApiResponse.ok("Mã xác nhận (OTP) đã được gửi đến email của bạn.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> xacNhanOtp(@Validated @RequestBody VerifyOtpRequest request) {
        boolean hopLe = userService.xacNhanOtp(request.getEmail(), request.getOtp());
        if (hopLe) {
            return ApiResponse.ok("Mã OTP hợp lệ.");
        } else {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> datLaiMatKhau(@Validated @RequestBody ResetPasswordRequest request) {
        userService.datLaiMatKhau(request);
        return ApiResponse.ok("Đặt lại mật khẩu thành công! Bạn có thể đăng nhập ngay bây giờ.");
    }
}
