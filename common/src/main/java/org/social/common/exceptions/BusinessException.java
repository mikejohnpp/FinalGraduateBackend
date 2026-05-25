package org.social.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends AppException {

    private final HttpStatus status;
    private final String customMessage;

    public BusinessException(String message) {
        super(ErrorCode.VALIDATION_FAILED);
        this.customMessage = message;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(HttpStatus status, String message) {
        super(ErrorCode.VALIDATION_FAILED);
        this.customMessage = message;
        this.status = status;
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
        this.customMessage = null;
        this.status = errorCode.getStatus();
    }

    @Override
    public String getMessage() {
        return customMessage != null ? customMessage : super.getMessage();
    }
}
