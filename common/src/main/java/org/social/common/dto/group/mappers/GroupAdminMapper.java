package org.social.common.dto.group.mappers;

import org.social.common.dto.group.views.GroupAdminInfoDTO;
import org.social.common.dto.group.views.MemberRequestDTO;
import org.social.common.dto.group.views.PendingPostDTO;
import org.social.common.entities.Group;
import org.social.common.entities.Post;
import org.social.common.entities.User;
import org.social.common.entities.UserGroup;

public class GroupAdminMapper {

    public static GroupAdminInfoDTO toAdminInfoDTO(Group group, long memberCount, String role) {
        return new GroupAdminInfoDTO(
                group.getId(),
                group.getName(),
                group.getAvatar(),
                group.getCoverPhoto(),
                group.getPrivacy(),
                memberCount,
                group.getDescription(),
                group.getCreatedAt(),
                role
        );
    }

    public static MemberRequestDTO toMemberRequestDTO(UserGroup userGroup) {
        User user = userGroup.getUser();
        return new MemberRequestDTO(
                user.getId(),
                user.getId(),
                user.getNickName() != null ? user.getNickName() : user.getUserName(),
                user.getAvatar(),
                userGroup.getRequestedAt(),
                user.getGender(),
                null
        );
    }

    public static PendingPostDTO toPendingPostDTO(Post post) {
        User author = post.getUser();
        return new PendingPostDTO(
                post.getId(),
                author.getId(),
                author.getNickName() != null ? author.getNickName() : author.getUserName(),
                author.getAvatar(),
                post.getContent(),
                post.getCreatedAt(),
                post.getStatus()
        );
    }
}
