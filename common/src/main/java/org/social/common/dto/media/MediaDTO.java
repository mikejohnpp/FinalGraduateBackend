package org.social.common.dto.media;

public record MediaDTO(
        Integer id,
        String url,
        String mediaType,
        Integer position) {
}
