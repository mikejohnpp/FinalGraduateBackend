package org.social.common.dto.friend.views;

import org.social.common.dto.user.views.AuthorDTO;

import java.time.Instant;

public record FriendRequestDTO(
        Integer requestId,
        AuthorDTO sender,
        int mutualFriendCount,
        Instant createdAt
) {}
