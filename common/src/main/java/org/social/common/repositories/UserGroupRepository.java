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

    @Query("SELECT ug FROM UserGroup ug WHERE ug.user.id = :userId AND ug.group.isActive = true AND ug.status = 'APPROVED'")
    List<UserGroup> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT ug FROM UserGroup ug WHERE ug.group.id = :groupId AND ug.status = 'APPROVED'")
    Page<UserGroup> findByGroupId(@Param("groupId") Integer groupId, Pageable pageable);

    long countByGroupIdAndStatus(Integer groupId, String status);

    Optional<UserGroup> findByUserIdAndGroupId(Integer userId, Integer groupId);

    boolean existsByUserIdAndGroupId(Integer userId, Integer groupId);

    @Query("SELECT ug FROM UserGroup ug JOIN FETCH ug.user WHERE ug.group.id = :groupId AND ug.status = :status")
    Page<UserGroup> findByGroupIdAndStatus(@Param("groupId") Integer groupId, @Param("status") String status, Pageable pageable);

    @Query("SELECT ug FROM UserGroup ug JOIN FETCH ug.user u WHERE ug.group.id = :groupId AND ug.status = :status " +
           "AND LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<UserGroup> findByGroupIdAndStatusAndSearch(@Param("groupId") Integer groupId, @Param("status") String status,
                                                    @Param("search") String search, Pageable pageable);

    @Query("SELECT ug FROM UserGroup ug JOIN FETCH ug.user u WHERE ug.group.id = :groupId AND ug.status = :status " +
           "AND LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%')) AND u.gender = :gender")
    Page<UserGroup> findByGroupIdAndStatusAndSearchAndGender(@Param("groupId") Integer groupId, @Param("status") String status,
                                                             @Param("search") String search, @Param("gender") String gender, Pageable pageable);

    @Query("SELECT ug FROM UserGroup ug JOIN FETCH ug.user u WHERE ug.group.id = :groupId AND ug.status = :status AND u.gender = :gender")
    Page<UserGroup> findByGroupIdAndStatusAndGender(@Param("groupId") Integer groupId, @Param("status") String status,
                                                    @Param("gender") String gender, Pageable pageable);

    @Query("SELECT ug FROM UserGroup ug WHERE ug.group.id = :groupId AND ug.user.id IN :userIds AND ug.status = :status")
    List<UserGroup> findByGroupIdAndUserIdsAndStatus(@Param("groupId") Integer groupId, @Param("userIds") List<Integer> userIds,
                                                     @Param("status") String status);
}
