package org.social.common.exceptions;

import lombok.Getter;

@Getter
public abstract class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private Object data;

    protected AppException(ErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
        this.data = null;
    }

    protected AppException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.formatMessage(args), cause);
        this.errorCode = errorCode;
        this.data = null;
    }

    @SuppressWarnings("unchecked")
    public <T extends AppException> T withData(Object data) {
        this.data = data;
        return (T) this;
    }
}
