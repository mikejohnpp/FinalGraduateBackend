package org.social.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception nghiệp vụ — ném ra khi có lỗi logic (email trùng, mật khẩu không khớp, v.v.)
 * Thay thế RuntimeException thô để mang thêm thông tin HTTP status.
 *
 * Cách dùng trong Service:
 *   throw new BusinessException("Email đã được sử dụng!");                          // mặc định 400
 *   throw new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");  // tùy status
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    /** Mặc định trả về 400 BAD_REQUEST */
    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    /** Tùy chọn HTTP status */
    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
