package org.social.userservice.services;

import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.group.requests.GroupCreateRequest;
import org.social.common.dto.group.views.GroupDTO;
import org.social.common.dto.group.views.GroupMemberDTO;
import org.social.common.dto.post.views.PostSummaryDTO;
import org.social.common.dto.admin.GroupAdminDTO;
import org.social.common.dto.admin.requests.AdminGroupUpdateRequest;
import org.social.common.dto.PageResponse;

import java.util.List;

public interface GroupService {
    GroupDTO create(GroupCreateRequest request, Integer userId);

    GroupDTO getById(Integer id, Integer userId);

    List<GroupDTO> getJoinedGroups(Integer userId);

    List<GroupDTO> getSuggestedGroups(Integer userId);

    org.social.common.dto.group.responses.JoinGroupResponse join(Integer groupId, Integer userId);

    void leave(Integer groupId, Integer userId);

    List<GroupMemberDTO> getMembers(Integer groupId);

    GroupDTO updateAvatar(Integer groupId, Integer userId, String avatarUrl);

    GroupDTO updateCover(Integer groupId, Integer userId, String coverUrl);

    CursorPageResponse<PostSummaryDTO> getGroupFeed(Integer userId, String cursor, int size);

    CursorPageResponse<PostSummaryDTO> getGroupPosts(Integer groupId, Integer userId, String cursor, int size);

    // Admin methods
    PageResponse<GroupAdminDTO> getAllGroups(int page, int size, String search);

    GroupAdminDTO createGroupAdmin(org.social.common.dto.admin.requests.AdminGroupCreateRequest request);

    GroupAdminDTO updateGroupAdmin(Integer id, AdminGroupUpdateRequest request);

    void deleteGroupAdmin(Integer id);
}
