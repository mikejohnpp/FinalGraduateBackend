package org.social.common.dto.friend.mappers;

import org.social.common.dto.friend.views.FriendRequestDTO;
import org.social.common.dto.friend.views.FriendSuggestionDTO;
import org.social.common.dto.friend.views.FriendshipDTO;
import org.social.common.dto.user.views.AuthorDTO;
import org.social.common.entities.User;
import org.social.common.entities.UserFriend;

public class FriendMapper {

    private static AuthorDTO toAuthorDTO(User user) {
        if (user == null) return null;
        return new AuthorDTO(
                user.getId(),
                user.getUserName(),
                user.getAvatar(),
                user.getNickName()
        );
    }

    public static FriendRequestDTO toFriendRequestDTO(UserFriend userFriend, int mutualFriendCount) {
        return new FriendRequestDTO(
                userFriend.getId().getUserId(),
                toAuthorDTO(userFriend.getUser()),
                mutualFriendCount,
                userFriend.getCreatedAt()
        );
    }

    public static FriendshipDTO toFriendshipDTO(UserFriend userFriend, int mutualFriendCount) {
        return new FriendshipDTO(
                toAuthorDTO(userFriend.getFriend()),
                userFriend.getCreatedAt(),
                mutualFriendCount
        );
    }

    public static FriendSuggestionDTO toFriendSuggestionDTO(User user, int mutualFriendCount) {
        return new FriendSuggestionDTO(
                toAuthorDTO(user),
                mutualFriendCount
        );
    }
}
