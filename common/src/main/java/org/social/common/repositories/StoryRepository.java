package org.social.common.repositories;


import org.social.common.entities.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Integer> {

    @Query("""
    SELECT s
    FROM Story s
    JOIN FETCH s.user
    WHERE s.type = 'REEL'
    ORDER BY s.createdAt DESC
""")
    Page<Story> findAllWithUser(Pageable pageable);

    @Query("""
    SELECT s
    FROM Story s
    JOIN FETCH s.user
    WHERE s.user.id = :userId
      AND s.type = 'REEL'
    ORDER BY s.createdAt DESC
""")
    Page<Story> findAllByUserId(
            @Param("userId") Integer userId,
            Pageable pageable
    );




    @Query("""
    SELECT DISTINCT s
    FROM Story s
    JOIN FETCH s.user
    LEFT JOIN UserFriend uf
        ON s.user.id = uf.friend.id
    WHERE s.type = 'STORY'
      AND (
            s.user.id = :userId
            OR (
                uf.user.id = :userId
                AND uf.status = 'ACCEPTED'
            )
      )
""")
    List<Story> findAllStoryFeed(@Param("userId") Integer userId);



}
