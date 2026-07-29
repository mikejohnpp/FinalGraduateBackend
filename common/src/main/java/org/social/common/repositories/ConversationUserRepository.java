package org.social.common.repositories;

import org.social.common.entities.ConversationUser;
import org.social.common.entities.ConversationUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationUserRepository extends JpaRepository<ConversationUser, ConversationUserId> {

    @Query("SELECT cu.id.userId FROM ConversationUser cu WHERE cu.id.conversationId = :conversationId")
    List<Integer> findUserIdsByConversationId(@Param("conversationId") Integer conversationId);
}

