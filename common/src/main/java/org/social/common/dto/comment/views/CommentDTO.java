package org.social.common.dto.comment.views;

import org.social.common.dto.user.views.AuthorDTO;
import java.time.Instant;

public record CommentDTO(
        Integer id,
        AuthorDTO author,
        Integer postId,
        Integer parentId,
        String content,
        int likeCount,
        int replyCount,
        boolean liked,
        Instant createdAt
) {}
