package org.social.userservice.services;

import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.group.requests.GroupCreateRequest;
import org.social.common.dto.group.views.GroupDTO;
import org.social.common.dto.group.views.GroupMemberDTO;
import org.social.common.dto.post.views.PostSummaryDTO;

import java.util.List;

public interface GroupService {
    GroupDTO create(GroupCreateRequest request, Integer userId);
    GroupDTO getById(Integer id, Integer userId);
    List<GroupDTO> getJoinedGroups(Integer userId);
    List<GroupDTO> getSuggestedGroups(Integer userId);
    void join(Integer groupId, Integer userId);
    void leave(Integer groupId, Integer userId);
    List<GroupMemberDTO> getMembers(Integer groupId);
    CursorPageResponse<PostSummaryDTO> getGroupFeed(Integer userId, String cursor, int size);
    CursorPageResponse<PostSummaryDTO> getGroupPosts(Integer groupId, Integer userId, String cursor, int size);
}
