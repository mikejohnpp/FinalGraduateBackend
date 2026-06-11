package org.social.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    RESOURCE_NOT_FOUND("Không tìm thấy {0} với id={1}", HttpStatus.NOT_FOUND),
    VALIDATION_FAILED("Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    DUPLICATE_ENTRY("{0} đã tồn tại", HttpStatus.CONFLICT),
    UNAUTHORIZED("Không có quyền truy cập", HttpStatus.UNAUTHORIZED),
    ACCOUNT_NOT_ACTIVATED("Tài khoản chưa được kích hoạt", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("Tài khoản đã bị khóa", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("Email hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("Token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    EXTERNAL_ERROR("Lỗi kết nối {0}", HttpStatus.BAD_GATEWAY),
    INTERNAL_ERROR("Lỗi hệ thống, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String messageTemplate;
    private final HttpStatus status;

    public String getCode() {
        return String.valueOf(status.value());
    }

    public String formatMessage(Object... args) {
        if (args == null || args.length == 0) {
            return messageTemplate;
        }
        return MessageFormat.format(messageTemplate, args);
    }
}
