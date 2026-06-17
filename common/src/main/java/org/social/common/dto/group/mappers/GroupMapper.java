package org.social.common.dto.group.mappers;

import org.social.common.dto.group.views.GroupDTO;
import org.social.common.dto.group.views.GroupMemberDTO;
import org.social.common.dto.group.views.GroupSummaryDTO;
import org.social.common.entities.Group;
import org.social.common.entities.UserGroup;

import java.util.ArrayList;

public class GroupMapper {

    public static GroupSummaryDTO toSummaryDTO(Group group) {
        if (group == null) return null;
        return new GroupSummaryDTO(
                group.getId(),
                group.getName(),
                group.getAvatar()
        );
    }

    public static GroupDTO toDTO(Group group, long memberCount, boolean isJoined, boolean isPending, String role) {
        if (group == null) return null;
        return new GroupDTO(
                group.getId(),
                group.getName(),
                group.getCoverPhoto(),
                group.getAvatar(),
                group.getPrivacy(),
                memberCount,
                null, // postFrequency - can be calculated later
                null, // lastAccessed
                isJoined,
                isPending,
                role,
                new ArrayList<>(), // mutualFriends
                0 // mutualFriendCount
        );
    }

    public static GroupMemberDTO toMemberDTO(UserGroup userGroup) {
        if (userGroup == null) return null;
        return new GroupMemberDTO(
                userGroup.getUser().getId(),
                userGroup.getUser().getNickName() != null ? userGroup.getUser().getNickName() : userGroup.getUser().getUserName(),
                userGroup.getUser().getAvatar(),
                userGroup.getRole()
        );
    }
}
