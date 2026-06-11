package org.social.common.dto.user.views;

public record AuthorDTO(
        Integer id,
        String name,
        String avatar,
        String nickName
) {
}
