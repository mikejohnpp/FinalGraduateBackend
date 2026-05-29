package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.group.mappers.GroupMapper;
import org.social.common.dto.group.requests.GroupCreateRequest;
import org.social.common.dto.group.views.GroupDTO;
import org.social.common.dto.group.views.GroupMemberDTO;
import org.social.common.dto.post.mappers.PostMapper;
import org.social.common.dto.post.views.PostSummaryDTO;
import org.social.common.entities.*;
import org.social.common.exceptions.ResourceNotFoundException;
import org.social.common.repositories.*;
import org.social.userservice.services.GroupService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Override
    @Transactional
    public GroupDTO create(GroupCreateRequest request, Integer userId) {
        User admin = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Group group = new Group();
        group.setName(request.name());
        group.setPrivacy(request.privacy() != null ? request.privacy() : "public");
        group.setAdmin(admin);
        group.setIsActive(true);
        Group savedGroup = groupRepository.save(group);

        UserGroup membership = new UserGroup();
        UserGroupId userGroupId = new UserGroupId();
        userGroupId.setUserId(userId);
        userGroupId.setGroupId(savedGroup.getId());
        membership.setId(userGroupId);
        membership.setUser(admin);
        membership.setGroup(savedGroup);
        membership.setRole("ADMIN");
        userGroupRepository.save(membership);

        return GroupMapper.toDTO(savedGroup, 1, true, "ADMIN");
    }

    @Override
    public GroupDTO getById(Integer id, Integer userId) {
        Group group = groupRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group", id));
        
        long memberCount = userGroupRepository.countByGroupId(id);
        Optional<UserGroup> membership = userGroupRepository.findByUserIdAndGroupId(userId, id);
        
        return GroupMapper.toDTO(
                group, 
                memberCount, 
                membership.isPresent(), 
                membership.map(UserGroup::getRole).orElse(null)
        );
    }

    @Override
    public List<GroupDTO> getJoinedGroups(Integer userId) {
        List<UserGroup> memberships = userGroupRepository.findByUserId(userId);
        return memberships.stream()
                .map(ug -> {
                    long count = userGroupRepository.countByGroupId(ug.getGroup().getId());
                    return GroupMapper.toDTO(ug.getGroup(), count, true, ug.getRole());
                })
                .toList();
    }

    @Override
    public List<GroupDTO> getSuggestedGroups(Integer userId) {
        // Simple suggestion: all active groups user hasn't joined yet
        List<Integer> joinedGroupIds = userGroupRepository.findByUserId(userId).stream()
                .map(ug -> ug.getGroup().getId())
                .toList();
        
        return groupRepository.findAll().stream()
                .filter(g -> Boolean.TRUE.equals(g.getIsActive()) && !joinedGroupIds.contains(g.getId()))
                .limit(10)
                .map(g -> {
                    long count = userGroupRepository.countByGroupId(g.getId());
                    return GroupMapper.toDTO(g, count, false, null);
                })
                .toList();
    }

    @Override
    @Transactional
    public void join(Integer groupId, Integer userId) {
        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            return;
        }

        UserGroup membership = new UserGroup();
        UserGroupId userGroupId = new UserGroupId();
        userGroupId.setUserId(userId);
        userGroupId.setGroupId(groupId);
        membership.setId(userGroupId);
        membership.setUser(user);
        membership.setGroup(group);
        membership.setRole("MEMBER");
        userGroupRepository.save(membership);
    }

    @Override
    @Transactional
    public void leave(Integer groupId, Integer userId) {
        UserGroupId id = new UserGroupId();
        id.setUserId(userId);
        id.setGroupId(groupId);
        userGroupRepository.deleteById(id);
    }

    @Override
    public List<GroupMemberDTO> getMembers(Integer groupId) {
        return userGroupRepository.findByGroupId(groupId, PageRequest.of(0, 100))
                .getContent().stream()
                .map(GroupMapper::toMemberDTO)
                .toList();
    }

    @Override
    public CursorPageResponse<PostSummaryDTO> getGroupFeed(Integer userId, String cursor, int size) {
        List<Integer> joinedGroupIds = userGroupRepository.findByUserId(userId).stream()
                .map(ug -> ug.getGroup().getId())
                .toList();

        if (joinedGroupIds.isEmpty()) {
            return new CursorPageResponse<>(List.of(), null, false);
        }

        Instant cursorInstant = (cursor != null) ? Instant.parse(cursor) : Instant.now();
        List<Post> posts = postRepository.findActivePostsByGroupIdsBefore(joinedGroupIds, cursorInstant, PageRequest.of(0, size + 1));

        boolean hasMore = posts.size() > size;
        List<Post> pageData = hasMore ? posts.subList(0, size) : posts;
        String nextCursor = pageData.isEmpty() ? null : pageData.getLast().getCreatedAt().toString();

        List<PostSummaryDTO> dtos = pageData.stream()
                .map(post -> PostMapper.toSummaryDTO(
                        post, 
                        postLikeRepository.countByPostId(post.getId()), 
                        userGroupRepository.findByUserIdAndGroupId(post.getUser().getId(), post.getGroup().getId()).map(UserGroup::getRole).orElse(null)
                ))
                .toList();
        return new CursorPageResponse<>(dtos, nextCursor, hasMore);
    }

    @Override
    public CursorPageResponse<PostSummaryDTO> getGroupPosts(Integer groupId, Integer userId, String cursor, int size) {
        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));

        // If the group is private, ensure the user is a member
        if ("private".equalsIgnoreCase(group.getPrivacy())) {
            if (!userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
                throw new org.social.common.exceptions.BusinessException(
                        org.social.common.exceptions.ErrorCode.FORBIDDEN, 
                        "Bạn không có quyền xem bài viết của nhóm kín này"
                );
            }
        }

        Instant cursorInstant = (cursor != null) ? Instant.parse(cursor) : Instant.now();
        List<Post> posts = postRepository.findActivePostsByGroupIdsBefore(List.of(groupId), cursorInstant, PageRequest.of(0, size + 1));

        boolean hasMore = posts.size() > size;
        List<Post> pageData = hasMore ? posts.subList(0, size) : posts;
        String nextCursor = pageData.isEmpty() ? null : pageData.getLast().getCreatedAt().toString();

        List<PostSummaryDTO> dtos = pageData.stream()
                .map(post -> PostMapper.toSummaryDTO(
                        post, 
                        postLikeRepository.countByPostId(post.getId()), 
                        userGroupRepository.findByUserIdAndGroupId(post.getUser().getId(), post.getGroup().getId()).map(UserGroup::getRole).orElse(null)
                ))
                .toList();

        return new CursorPageResponse<>(dtos, nextCursor, hasMore);
    }
}
