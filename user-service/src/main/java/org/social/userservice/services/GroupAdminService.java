package org.social.userservice.services;

import org.social.common.dto.PageResponse;
import org.social.common.dto.group.views.*;

public interface GroupAdminService {

    GroupAdminInfoDTO getGroupInfo(Integer groupId, Integer userId);

    GroupStatsDTO getGroupStats(Integer groupId, Integer userId);

    PageResponse<MemberRequestDTO> getMemberRequests(Integer groupId, Integer userId,
                                                      String search, String gender, String sort,
                                                      int page, int size);

    void approveMemberRequests(Integer groupId, Integer userId, java.util.List<Integer> requestIds);

    void rejectMemberRequests(Integer groupId, Integer userId, java.util.List<Integer> requestIds);

    PageResponse<PendingPostDTO> getPendingPosts(Integer groupId, Integer userId, int page, int size);

    void approvePost(Integer groupId, Integer userId, Integer postId);

    void rejectPost(Integer groupId, Integer userId, Integer postId);
}
