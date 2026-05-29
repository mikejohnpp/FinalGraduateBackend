package org.social.common.dto.group.views;

import java.time.Instant;
import java.util.List;

public record GroupDTO(
        Integer id,
        String name,
        String coverPhoto,
        String avatar,
        String privacy,
        long memberCount,
        String postFrequency,
        Instant lastAccessed,
        boolean isJoined,
        String role,
        List<String> mutualFriends,
        long mutualFriendCount
) {}
