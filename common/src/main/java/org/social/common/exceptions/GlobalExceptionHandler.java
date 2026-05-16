package org.social.common.exceptions;

import org.social.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Bắt tất cả exception từ mọi controller trong ứng dụng.
 * Không cần try-catch trong controller nữa.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt lỗi validation từ @Valid / @Validated trên @RequestBody.
     *
     * Ví dụ JSON trả về khi nhập sai:
     * {
     *   "success": false,
     *   "message": "Dữ liệu không hợp lệ",
     *   "data": ["Email không được để trống", "Mật khẩu phải có ít nhất 6 ký tự"]
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        return ApiResponse.error(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", errors);
    }

    /**
     * Bắt lỗi nghiệp vụ từ BusinessException (email trùng, mật khẩu không khớp, v.v.)
     *
     * Ví dụ JSON trả về:
     * {
     *   "success": false,
     *   "message": "Email đã được sử dụng!"
     * }
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        return ApiResponse.error(ex.getStatus(), ex.getMessage());
    }

    /**
     * Bắt tất cả lỗi không mong muốn còn lại (fallback).
     *
     * Ví dụ JSON trả về:
     * {
     *   "success": false,
     *   "message": "Lỗi hệ thống, vui lòng thử lại sau."
     * }
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        ex.printStackTrace(); // In log để debug
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau.");
    }
}
