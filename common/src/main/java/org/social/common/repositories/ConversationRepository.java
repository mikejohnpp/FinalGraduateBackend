package org.social.common.repositories;


import org.social.common.entities.Conversation;
import org.social.common.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    @Query("""
    SELECT c FROM Conversation c
    JOIN c.user m
    WHERE c.isGroup = false
    AND m IN (:user1, :user2)
    GROUP BY c
    HAVING COUNT(m) = 2
""")
    Optional<Conversation> findPrivateConversation(
            @Param("user1") User user1,
            @Param("user2") User user2
    );
}
