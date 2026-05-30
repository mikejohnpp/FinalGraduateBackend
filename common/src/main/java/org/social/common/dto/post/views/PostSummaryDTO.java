package org.social.common.dto.post.views;

import org.social.common.dto.group.views.GroupSummaryDTO;
import org.social.common.dto.user.views.AuthorDTO;

import java.time.Instant;

public record PostSummaryDTO(
        Integer id,
        AuthorDTO author,
        String authorRole,
        Boolean isGroupPosted,
        GroupSummaryDTO group,
        Instant createdAt,
        int commentCount,
        String content,
        long likeCount,
        Boolean hasLiked
) {}
