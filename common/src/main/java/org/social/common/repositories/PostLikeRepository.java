package org.social.common.repositories;

import org.social.common.entities.PostLike;
import org.social.common.entities.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    long countByPostId(Integer postId);

    boolean existsByUserIdAndPostId(Integer userId, Integer postId);

    void deleteByUserIdAndPostId(Integer userId, Integer postId);

    void deleteByPostId(Integer postId);

    @Query("SELECT pl.postId FROM PostLike pl WHERE pl.userId = :userId AND pl.postId IN :postIds")
    java.util.List<Integer> findPostIdsByUserIdAndPostIdIn(
        @Param("userId") Integer userId, 
        @Param("postIds") java.util.List<Integer> postIds
    );

    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.group.id = :groupId AND pl.createdAt >= :since")
    long countByGroupIdAndCreatedAtAfter(@Param("groupId") Integer groupId, @Param("since") Instant since);
}
