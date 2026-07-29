package org.social.common.dto.friend.views;

public record FriendStatusDTO(
        FriendRelationStatus status,
        Integer requestId
) {}
