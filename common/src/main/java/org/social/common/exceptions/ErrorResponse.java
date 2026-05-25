package org.social.common.exceptions;

import lombok.Builder;

@Builder
public record ErrorResponse(
        boolean success,
        String message,
        Object data,
        String code
) {
}
