package org.social.common.repositories;

import org.social.common.entities.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId AND c.parent IS NULL AND c.isActive = true AND c.createdAt < :cursor ORDER BY c.createdAt DESC")
    List<Comment> findActiveCommentsBefore(Integer postId, Instant cursor, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId AND c.parent.id = :parentId AND c.isActive = true AND c.createdAt > :cursor ORDER BY c.createdAt ASC")
    List<Comment> findActiveRepliesAfter(Integer postId, Integer parentId, Instant cursor, Pageable pageable);

    Optional<Comment> findByIdAndIsActiveTrue(Integer id);

    List<Comment> findAllByParentIdAndIsActiveTrue(Integer parentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.group.id = :groupId AND c.isActive = true AND c.createdAt >= :since")
    long countByGroupIdAndCreatedAtAfter(@org.springframework.data.repository.query.Param("groupId") Integer groupId,
                                         @org.springframework.data.repository.query.Param("since") Instant since);
}