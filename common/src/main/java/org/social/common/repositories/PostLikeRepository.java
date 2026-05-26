package org.social.common.repositories;

import org.social.common.entities.PostLike;
import org.social.common.entities.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    long countByPostId(Integer postId);

    boolean existsByUserIdAndPostId(Integer userId, Integer postId);

    void deleteByUserIdAndPostId(Integer userId, Integer postId);

    void deleteByPostId(Integer postId);
}
