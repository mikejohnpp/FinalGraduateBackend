package org.social.common.repositories;

import org.social.common.entities.Message;
import org.social.common.entities.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationId(
            Integer conversationId,
            Pageable pageable
    );

    List<Message> findByConversationIdAndMessageTypeIn(
            Integer conversationId,
            List<MessageType> messageTypes
    );

    @Query(value = """
        SELECT *
        FROM messages
        WHERE conversation_id = :conversationId
          AND id < :beforeId
        ORDER BY id DESC
        LIMIT :limit
        """,
            nativeQuery = true)
    List<Message> findOlderMessages(
            @Param("conversationId") Integer conversationId,
            @Param("beforeId") Long beforeId,
            @Param("limit") Integer limit);
}
