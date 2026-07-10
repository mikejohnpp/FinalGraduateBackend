package org.social.common.dto.comment.views;

import org.social.common.dto.media.MediaDTO;
import org.social.common.dto.user.views.AuthorDTO;

import java.time.Instant;
import java.util.List;

public record CommentDTO(
                Integer id,
                AuthorDTO author,
                Integer postId,
                Integer parentId,
                String content,
                int likeCount,
                int replyCount,
                boolean liked,
                Instant createdAt,
                String sentiment,
                Double confidence,
                String cancelReason,
                List<MediaDTO> media) {
}
