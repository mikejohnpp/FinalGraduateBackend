package org.social.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Cấu trúc response chuẩn dùng cho toàn bộ API.
 *
 * JSON trả về:
 * {
 *   "success": true,
 *   "message": "...",
 *   "data": { ... }   // null thì bị bỏ qua trong JSON
 * }
 *
 * Cách dùng:
 *   return ApiResponse.ok("Thành công");
 *   return ApiResponse.ok("Lấy danh sách thành công", danhSach);
 *   return ApiResponse.error(HttpStatus.BAD_REQUEST, "Email đã tồn tại");
 *   return ApiResponse.error(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // Không serialize field null (data)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;


    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // ================================================================
    // SUCCESS — 200 OK
    // ================================================================

    /** Thành công, chỉ có message, không có data */
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null));
    }

    /** Thành công, có cả message lẫn data */
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, data));
    }

    // ================================================================
    // ERROR — Tùy HTTP status
    // ================================================================

    /** Lỗi với HTTP status tùy chọn */
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(false, message, null));
    }

    /** Lỗi kèm thêm data (ví dụ: danh sách lỗi validation) */
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message, T data) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(false, message, data));
    }
}
