package org.social.common.repositories;

import org.social.common.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer>, JpaSpecificationExecutor<Post> {

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.isActive = true AND (p.status = 'APPROVED' OR p.status IS NULL)")
    List<Post> findAllWithUser();

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.isActive = true AND (p.status = 'APPROVED' OR p.status IS NULL) AND p.createdAt < :cursor ORDER BY p.createdAt DESC")
    List<Post> findActivePostsBefore(@Param("cursor") Instant cursor, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.isActive = true AND (p.status = 'APPROVED' OR p.status IS NULL) AND p.group.id IN :groupIds AND p.createdAt < :cursor ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByGroupIdsBefore(@Param("groupIds") List<Integer> groupIds,
            @Param("cursor") Instant cursor, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "user" })
    Page<Post> findAll(Specification<Post> spec, Pageable pageable);

    List<Post> findByUser_IdAndIsActiveTrue(Integer userId);

    Optional<Post> findByIdAndIsActiveTrue(Integer id);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.group.id = :groupId AND p.status = :status AND p.isActive = true ORDER BY p.createdAt DESC")
    Page<Post> findByGroupIdAndStatusAndIsActiveTrue(@Param("groupId") Integer groupId, @Param("status") String status,
            Pageable pageable);

    long countByGroupIdAndStatusAndIsActiveTrue(Integer groupId, String status);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.group.id = :groupId AND p.isActive = true AND (p.status = 'APPROVED' OR p.status IS NULL) AND p.createdAt >= :since")
    long countByGroupIdAndCreatedAtAfter(@Param("groupId") Integer groupId, @Param("since") Instant since);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.group.id = :groupId AND p.isActive = true AND (p.status = 'APPROVED' OR p.status IS NULL) AND p.createdAt >= :start AND p.createdAt < :end")
    long countByGroupIdAndCreatedAtBetween(@Param("groupId") Integer groupId, @Param("start") Instant start,
            @Param("end") Instant end);

    long countByIsActiveTrue();
}
