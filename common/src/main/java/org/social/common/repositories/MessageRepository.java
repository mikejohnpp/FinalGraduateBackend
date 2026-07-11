package org.social.common.repositories;

import org.social.common.entities.Message;
import org.social.common.entities.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    Page<Message> findByConversationId(
            Integer conversationId,
            Pageable pageable
    );

    List<Message> findByConversationIdAndMessageTypeIn(
            Integer conversationId,
            List<MessageType> messageTypes
    );
}
