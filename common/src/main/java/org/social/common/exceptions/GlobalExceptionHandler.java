package org.social.common.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest req) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("[{}] {} — path={}", errorCode.getCode(), ex.getMessage(), req.getRequestURI());
        return buildResponse(errorCode.getStatus(), errorCode.getCode(), ex.getMessage(), req, ex.getData());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> Objects.requireNonNullElse(fe.getDefaultMessage(), "invalid"),
                        (a, b) -> a
                ));
        log.warn("[{}] Validation failed — path={}, errors={}", ErrorCode.VALIDATION_FAILED.getCode(), req.getRequestURI(), fieldErrors);
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED.getCode(), "Dữ liệu không hợp lệ", req, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations()
                .forEach(cv -> fieldErrors.put(cv.getPropertyPath().toString(), cv.getMessage()));
        log.warn("[{}] Constraint violation — path={}, errors={}", ErrorCode.VALIDATION_FAILED.getCode(), req.getRequestURI(), fieldErrors);
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED.getCode(), "Dữ liệu không hợp lệ", req, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn("Không thể đọc request body: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED.getCode(), "Request body không đúng định dạng", req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        log.warn("Kiểu dữ liệu không hợp lệ - tham số '{}': {}", ex.getName(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED.getCode(), "Kiểu dữ liệu của tham số '" + ex.getName() + "' không hợp lệ", req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        log.warn("Thiếu request parameter: {}", ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED.getCode(), "Thiếu tham số bắt buộc: " + ex.getParameterName(), req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        log.warn("Content-Type không hỗ trợ: {}", ex.getContentType());
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCode.VALIDATION_FAILED.getCode(), "Content-Type không được hỗ trợ", req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        log.warn("Method không được hỗ trợ: {}", ex.getMethod());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.VALIDATION_FAILED.getCode(), "HTTP method không được hỗ trợ", req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest req) {
        log.warn("Không tìm thấy route: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.getCode(), "Không tìm thấy tài nguyên yêu cầu", req, null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        log.warn("ResponseStatusException: {} - {}", ex.getStatusCode(), ex.getReason());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return buildResponse(status, ErrorCode.INTERNAL_ERROR.getCode(), ex.getReason(), req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.error("Vi phạm ràng buộc database: {}", ex.getMostSpecificCause().getMessage());
        return buildResponse(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_ENTRY.getCode(), "Dữ liệu đã tồn tại hoặc vi phạm ràng buộc", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest req) {
        log.error("Lỗi không xác định tại {}: ", req.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.getCode(), "Lỗi hệ thống, vui lòng thử lại sau", req, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message,
                                                         HttpServletRequest req, Object data) {
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .code(code)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
