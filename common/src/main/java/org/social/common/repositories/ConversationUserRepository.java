package org.social.common.repositories;

import org.social.common.entities.ConversationUser;
import org.social.common.entities.ConversationUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationUserRepository extends JpaRepository<ConversationUser, ConversationUserId> {
}
