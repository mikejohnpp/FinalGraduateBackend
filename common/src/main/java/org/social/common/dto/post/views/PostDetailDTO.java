package org.social.common.dto.post.views;

import org.social.common.dto.user.views.AuthorDTO;

import java.time.Instant;

public record PostDetailDTO(
        Integer id,
        AuthorDTO author,
        Boolean isGroupPosted,
        Instant createdAt,
        String content,
        long likeCount
) {}
