package org.social.common.dto.friend.views;

import org.social.common.dto.user.views.AuthorDTO;

public record FriendSuggestionDTO(
        AuthorDTO user,
        int mutualFriendCount
) {}
