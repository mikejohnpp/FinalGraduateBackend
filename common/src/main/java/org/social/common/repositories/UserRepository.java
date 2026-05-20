package org.social.common.repositories;

import org.social.common.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    java.util.Optional<User> findByActiveCode(String activeCode);

    @EntityGraph(attributePaths = "role")
    java.util.Optional<User> findByEmail(String email);
}
