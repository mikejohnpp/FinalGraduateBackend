package org.social.apigateway.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.social.common.exceptions.BusinessException;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(-1)
public class SecurityExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS.getCode(), "Email không tồn tại.", req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS.getCode(), "Mật khẩu không đúng.", req);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ErrorResponse> handleInternalAuthException(InternalAuthenticationServiceException ex, HttpServletRequest req) {
        if (ex.getCause() instanceof BusinessException bex) {
            return buildResponse(HttpStatus.UNAUTHORIZED, bex.getErrorCode().getCode(), bex.getMessage(), req);
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.getCode(), "Lỗi hệ thống, vui lòng thử lại sau.", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthenticationException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getCode(), "Đăng nhập thất bại.", req);
    }

    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(io.jsonwebtoken.JwtException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN.getCode(), "Token không hợp lệ hoặc đã hết hạn.", req);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .code(code)
                .message(message)
                .data(null)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
