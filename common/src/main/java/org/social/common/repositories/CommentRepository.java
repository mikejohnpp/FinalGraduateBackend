package org.social.common.repositories;

import org.social.common.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Message, Integer> {
    List<Message> findByConversation_NameLikeAndConversation_IsGroupTrue(String name);
}