package org.social.common.repositories;

import org.social.common.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);

    Optional<User> findByActiveCode(String activeCode);

    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "role")
    Optional<User> findByIdAndIsActiveTrue(Long id);

    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmailAndIsActiveTrue(String email);
}

