package org.social.common.dto.post.mappers;

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

    public static PostDTO toPostDTO(Post post) {
        return new PostDTO(
                post.getId(),
                toAuthorDTO(post.getUser()),
                post.getIsGroupPosted(),
                post.getCreatedAt(),
                post.getContent(),
                0L
        );
    }

    public static PostSummaryDTO toSummaryDTO(Post post, long likeCount) {
        int commentCount = post.getComments() != null ? post.getComments().size() : 0;
        return new PostSummaryDTO(
                post.getId(),
                toAuthorDTO(post.getUser()),
                post.getIsGroupPosted(),
                post.getCreatedAt(),
                commentCount,
                post.getContent(),
                likeCount
        );
    }

    public static PostDetailDTO toDetailDTO(Post post, long likeCount) {
        return new PostDetailDTO(
                post.getId(),
                toAuthorDTO(post.getUser()),
                post.getIsGroupPosted(),
                post.getCreatedAt(),
                post.getContent(),
                likeCount
        );
    }
}
