package org.social.config;


import org.social.handler.ResponseStatus;

import java.time.LocalDateTime;

public class ResponseApi<T> {
    final int code;
    final String message;
    final T data;
    final LocalDateTime timestamp;
    public ResponseApi(ResponseStatus responseStatus, T data) {
        this.code = responseStatus.getCode();
        this.message = responseStatus.getMessage();
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public T getData() {
        return data;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    @Override
    public String toString() {
        return "ResponseApi{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }

}
