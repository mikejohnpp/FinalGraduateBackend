package org.social.common.dto.post.mappers;

import org.social.common.dto.group.mappers.GroupMapper;
import org.social.common.dto.post.views.PostDetailDTO;
import org.social.common.dto.post.views.PostDTO;
import org.social.common.dto.post.views.PostSummaryDTO;
import org.social.common.dto.user.views.AuthorDTO;
import org.social.common.entities.Post;
import org.social.common.entities.User;

public class PostMapper {

    private static AuthorDTO toAuthorDTO(User user) {
        if (user == null) return null;
        return new AuthorDTO(
                user.getId(),
                user.getUserName(),
                user.getAvatar(),
                user.getNickName()
        );
    }

    public static PostDTO toPostDTO(Post post, String authorRole, boolean hasLiked) {
        return new PostDTO(
                post.getId(),
                toAuthorDTO(post.getUser()),
                authorRole,
                post.getIsGroupPosted(),
                GroupMapper.toSummaryDTO(post.getGroup()),
                post.getCreatedAt(),
                post.getContent(),
                0L,
                hasLiked,
                post.getSentiment(),
                post.getConfidence(),
                post.getCancelReason(),
                post.getStatus()
        );
    }

    public static PostSummaryDTO toSummaryDTO(Post post, long likeCount, String authorRole, boolean hasLiked) {
        int commentCount = post.getCommentCount() != null ? post.getCommentCount() : 0;
        long currentLikeCount = post.getLikeCount() != null ? post.getLikeCount() : likeCount;
        return new PostSummaryDTO(
                post.getId(),
                toAuthorDTO(post.getUser()),
                authorRole,
                post.getIsGroupPosted(),
                GroupMapper.toSummaryDTO(post.getGroup()),
                post.getCreatedAt(),
                commentCount,
                post.getContent(),
                currentLikeCount,
                hasLiked,
                post.getSentiment(),
                post.getConfidence(),
                post.getCancelReason()
        );
    }

    public static PostDetailDTO toDetailDTO(Post post, long likeCount, String authorRole, boolean hasLiked) {
        long currentLikeCount = post.getLikeCount() != null ? post.getLikeCount() : likeCount;
        return new PostDetailDTO(
                post.getId(),
                toAuthorDTO(post.getUser()),
                authorRole,
                post.getIsGroupPosted(),
                GroupMapper.toSummaryDTO(post.getGroup()),
                post.getCreatedAt(),
                post.getContent(),
                currentLikeCount,
                hasLiked,
                post.getSentiment(),
                post.getConfidence(),
                post.getCancelReason()
        );
    }
}
