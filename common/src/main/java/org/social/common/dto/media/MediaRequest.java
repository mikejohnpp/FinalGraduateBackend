package org.social.common.dto.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MediaRequest(
        @NotBlank(message = "URL media không được để trống") @Size(max = 1000, message = "URL media không được vượt quá 1000 ký tự") String url,
        String mediaType,
        Integer position) {
}
