package org.social.common.repositories;

import org.social.common.entities.FriendStatus;
import org.social.common.entities.UserFriend;
import org.social.common.entities.UserFriendId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserFriendRepository extends JpaRepository<UserFriend, UserFriendId> {

    Optional<UserFriend> findByIdUserIdAndIdFriendIdAndStatus(Integer userId, Integer friendId, FriendStatus status);

    boolean existsByIdUserIdAndIdFriendIdAndStatus(Integer userId, Integer friendId, FriendStatus status);

    @Query("SELECT uf FROM UserFriend uf JOIN FETCH uf.user WHERE uf.id.friendId = :userId AND uf.status = 'PENDING' AND uf.createdAt < :cursor ORDER BY uf.createdAt DESC")
    List<UserFriend> findPendingRequestsBefore(Integer userId, Instant cursor, Pageable pageable);

    @Query("SELECT uf FROM UserFriend uf JOIN FETCH uf.user WHERE uf.id.userId = :userId AND uf.status = 'ACCEPTED' AND uf.createdAt < :cursor ORDER BY uf.createdAt DESC")
    List<UserFriend> findAcceptedFriendsBefore(Integer userId, Instant cursor, Pageable pageable);

    @Query("SELECT COUNT(uf) FROM UserFriend uf WHERE uf.id.friendId = :userId AND uf.status = 'PENDING'")
    int countPendingRequests(Integer userId);

    @Query("SELECT uf.id.friendId FROM UserFriend uf WHERE uf.id.userId = :userId AND uf.status = 'ACCEPTED'")
    List<Integer> findAcceptedFriendIds(Integer userId);

    @Query("SELECT COUNT(uf1) FROM UserFriend uf1 JOIN UserFriend uf2 ON uf1.id.friendId = uf2.id.friendId WHERE uf1.id.userId = :userId AND uf2.id.userId = :otherId AND uf1.status = 'ACCEPTED' AND uf2.status = 'ACCEPTED'")
    int countMutualFriends(Integer userId, Integer otherId);

    @Modifying
    @Query("DELETE FROM UserFriend uf WHERE uf.id.userId = :userId AND uf.id.friendId = :friendId")
    void deleteByUserIdAndFriendId(Integer userId, Integer friendId);
}
