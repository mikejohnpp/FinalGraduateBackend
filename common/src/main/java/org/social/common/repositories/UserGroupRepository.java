package org.social.common.repositories;

import org.social.common.entities.UserGroup;
import org.social.common.entities.UserGroupId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, UserGroupId> {

    @Query("SELECT ug FROM UserGroup ug WHERE ug.user.id = :userId AND ug.group.isActive = true")
    List<UserGroup> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT ug FROM UserGroup ug WHERE ug.group.id = :groupId")
    Page<UserGroup> findByGroupId(@Param("groupId") Integer groupId, Pageable pageable);

    long countByGroupId(Integer groupId);

    Optional<UserGroup> findByUserIdAndGroupId(Integer userId, Integer groupId);

    boolean existsByUserIdAndGroupId(Integer userId, Integer groupId);
}
