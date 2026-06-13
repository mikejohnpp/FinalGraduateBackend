package org.social.common.repositories;

import org.social.common.entities.Group;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer>, JpaSpecificationExecutor<Group> {
    Optional<Group> findByIdAndIsActiveTrue(Integer id);

    List<Group> findByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
