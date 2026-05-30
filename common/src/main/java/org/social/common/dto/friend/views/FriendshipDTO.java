package org.social.common.dto.friend.views;

import org.social.common.dto.user.views.AuthorDTO;

import java.time.Instant;

public record FriendshipDTO(
        AuthorDTO user,
        Instant friendSince,
        int mutualFriendCount
) {}
