package org.social.chatservice.services;

import java.util.List;
public interface ConversationMemberCache {

    List<Integer> getMemberIds(Integer conversationId);

    void evict(Integer conversationId);
}
