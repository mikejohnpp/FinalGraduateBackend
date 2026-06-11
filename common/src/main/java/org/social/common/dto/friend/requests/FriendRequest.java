package org.social.common.dto.friend.requests;

import jakarta.validation.constraints.NotNull;

public record FriendRequest(
        @NotNull Integer userId,
        @NotNull Integer targetUserId
) {}
