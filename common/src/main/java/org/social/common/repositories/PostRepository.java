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

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.isActive = true")
    List<Post> findAllWithUser();

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.isActive = true AND p.createdAt < :cursor ORDER BY p.createdAt DESC")
    List<Post> findActivePostsBefore(@Param("cursor") Instant cursor, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<Post> findAll(Specification<Post> spec, Pageable pageable);

    List<Post> findByUser_IdAndIsActiveTrue(Integer userId);

    Optional<Post> findByIdAndIsActiveTrue(Integer id);
}
