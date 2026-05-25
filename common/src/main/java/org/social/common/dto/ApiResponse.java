package org.social.common.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final int code;

    private ApiResponse(boolean success, String message, int code, T data) {
        this.success = success;
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, HttpStatus.OK.value() ,null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, HttpStatus.OK.value(), data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, message, HttpStatus.CREATED.value(), null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, message, HttpStatus.CREATED.value(), data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> deleted(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, HttpStatus.OK.value(), null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(false, message, status.value() ,null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message, T data) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(false, message, status.value(), data));
    }
}
