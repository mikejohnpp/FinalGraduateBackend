package org.social.common.repositories;

import org.social.common.entities.CommentLike;
import org.social.common.entities.CommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {
    boolean existsByCommentIdAndUserId(Integer commentId, Integer userId);
    void deleteByCommentIdAndUserId(Integer commentId, Integer userId);
}
