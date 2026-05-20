package org.social.common.dto.post.mappers;

import org.social.common.dto.post.views.PostSummaryDTO;
import org.social.common.entities.Post;
import org.social.common.entities.PostDetail;
import org.social.common.dto.post.views.PostDTO;
import org.social.common.dto.post.views.PostDetailDTO;

import java.util.List;

public class PostMapper {

    public static PostDTO toPostDTO(Post post) {
        return new PostDTO(
                post.getId(),
                post.getUser().getUserName(),
                post.getIsGroupPosted(),
                post.getCreatedAt()
        );
    }

    public static PostSummaryDTO toSummaryDTO(Post post) {
        int commentCount = post.getComments() != null ? post.getComments().size() : 0;
        return new PostSummaryDTO(
                post.getId(),
                post.getUser().getUserName(),
                post.getIsGroupPosted(),
                post.getCreatedAt(),
                commentCount
        );
    }

    public static PostDetailDTO toDetailDTO(Post post, List<PostDetail> details) {
        List<String> contents = details.stream()
                .map(PostDetail::getContent)
                .toList();
        return new PostDetailDTO(
                post.getId(),
                post.getUser().getUserName(),
                post.getIsGroupPosted(),
                post.getCreatedAt(),
                contents
        );
    }
}
