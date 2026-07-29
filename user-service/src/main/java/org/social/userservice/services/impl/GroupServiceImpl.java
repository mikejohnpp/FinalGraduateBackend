package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.group.mappers.GroupMapper;
import org.social.common.dto.group.requests.GroupCreateRequest;
import org.social.common.dto.group.views.GroupDTO;
import org.social.common.dto.group.views.GroupMemberDTO;
import org.social.common.dto.post.mappers.PostMapper;
import org.social.common.dto.post.views.PostSummaryDTO;
import org.social.common.dto.admin.GroupAdminDTO;
import org.social.common.dto.admin.requests.AdminGroupUpdateRequest;
import org.social.common.dto.PageResponse;
import org.social.common.entities.*;
import org.social.common.exceptions.BusinessException;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ResourceNotFoundException;
import org.social.common.events.NotificationEvent;
import org.social.common.repositories.*;
import org.social.userservice.messaging.publishers.NotificationProducer;
import org.social.userservice.services.GroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        private final NotificationProducer notificationProducer;

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
                membership.setStatus("APPROVED");
                membership.setRequestedAt(Instant.now());
                userGroupRepository.save(membership);

                return GroupMapper.toDTO(savedGroup, 1, true, false, "ADMIN");
        }

        @Override
        public GroupDTO getById(Integer id, Integer userId) {
                Group group = groupRepository.findByIdAndIsActiveTrue(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Group", id));

                long memberCount = userGroupRepository.countByGroupIdAndStatus(id, "APPROVED");
                Optional<UserGroup> membership = userGroupRepository.findByUserIdAndGroupId(userId, id);

                boolean isJoined = membership.map(ug -> "APPROVED".equals(ug.getStatus())).orElse(false);
                boolean isPending = membership.map(ug -> "PENDING".equals(ug.getStatus())).orElse(false);

                return GroupMapper.toDTO(
                                group,
                                memberCount,
                                isJoined,
                                isPending,
                                membership.map(UserGroup::getRole).orElse(null));
        }

        @Override
        public List<GroupDTO> getJoinedGroups(Integer userId) {
                List<UserGroup> memberships = userGroupRepository.findByUserId(userId);
                return memberships.stream()
                                .map(ug -> {
                                        long count = userGroupRepository.countByGroupIdAndStatus(ug.getGroup().getId(),
                                                        "APPROVED");
                                        boolean isPending = "PENDING".equals(ug.getStatus());
                                        boolean isJoined = "APPROVED".equals(ug.getStatus());
                                        return GroupMapper.toDTO(ug.getGroup(), count, isJoined, isPending,
                                                        ug.getRole());
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
                                .filter(g -> Boolean.TRUE.equals(g.getIsActive())
                                                && !joinedGroupIds.contains(g.getId()))
                                .limit(10)
                                .map(g -> {
                                        long count = userGroupRepository.countByGroupIdAndStatus(g.getId(), "APPROVED");
                                        return GroupMapper.toDTO(g, count, false, false, null);
                                })
                                .toList();
        }

        @Override
        @Transactional
        public org.social.common.dto.group.responses.JoinGroupResponse join(Integer groupId, Integer userId) {
                Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
                User user = userRepository.findById(Long.valueOf(userId))
                                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

                Optional<UserGroup> existingMembership = userGroupRepository.findByUserIdAndGroupId(userId, groupId);
                if (existingMembership.isPresent()) {
                        return new org.social.common.dto.group.responses.JoinGroupResponse(
                                        existingMembership.get().getStatus());
                }

                boolean isPrivate = "PRIVATE".equalsIgnoreCase(group.getPrivacy());

                UserGroup membership = new UserGroup();
                UserGroupId userGroupId = new UserGroupId();
                userGroupId.setUserId(userId);
                userGroupId.setGroupId(groupId);
                membership.setId(userGroupId);
                membership.setUser(user);
                membership.setGroup(group);
                membership.setRole("MEMBER");
                String finalStatus = isPrivate ? "PENDING" : "APPROVED";
                membership.setStatus(finalStatus);
                membership.setRequestedAt(Instant.now());
                userGroupRepository.save(membership);

                // Nhóm kín: gửi thông báo GROUP_JOIN_REQUEST cho admin nhóm để duyệt
                if (isPrivate && group.getAdmin() != null) {
                        notificationProducer.publish(new NotificationEvent(
                                        group.getAdmin().getId(),
                                        userId,
                                        NotificationType.GROUP_JOIN_REQUEST.name(),
                                        "GROUP",
                                        groupId,
                                        null,
                                        "/groups/" + groupId));
                }

                return new org.social.common.dto.group.responses.JoinGroupResponse(finalStatus);
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
        @Transactional
        public GroupDTO updateAvatar(Integer groupId, Integer userId, String avatarUrl) {
                return updateImage(groupId, userId, avatarUrl, true);
        }

        @Override
        @Transactional
        public GroupDTO updateCover(Integer groupId, Integer userId, String coverUrl) {
                return updateImage(groupId, userId, coverUrl, false);
        }

        private GroupDTO updateImage(Integer groupId, Integer userId, String imageUrl, boolean isAvatar) {
                Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));

                UserGroup membership = userGroupRepository.findByUserIdAndGroupId(userId, groupId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN,
                                                "Bạn không có quyền chỉnh sửa nhóm này"));
                if (!"ADMIN".equals(membership.getRole())) {
                        throw new BusinessException(ErrorCode.FORBIDDEN,
                                        "Chỉ quản trị viên nhóm mới được cập nhật ảnh");
                }

                if (isAvatar) {
                        group.setAvatar(imageUrl);
                } else {
                        group.setCoverPhoto(imageUrl);
                }
                groupRepository.save(group);

                long memberCount = userGroupRepository.countByGroupIdAndStatus(groupId, "APPROVED");
                return GroupMapper.toDTO(group, memberCount, true, false, membership.getRole());
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
                List<Post> posts = postRepository.findActivePostsByGroupIdsBefore(joinedGroupIds, cursorInstant,
                                PageRequest.of(0, size + 1));

                boolean hasMore = posts.size() > size;
                List<Post> pageData = hasMore ? posts.subList(0, size) : posts;
                String nextCursor = pageData.isEmpty() ? null : pageData.getLast().getCreatedAt().toString();

                List<Integer> postIds = pageData.stream().map(Post::getId).toList();
                List<Integer> likedPostIds = (userId != null && !postIds.isEmpty())
                                ? postLikeRepository.findPostIdsByUserIdAndPostIdIn(userId, postIds)
                                : List.of();

                List<PostSummaryDTO> dtos = pageData.stream()
                                .map(post -> PostMapper.toSummaryDTO(
                                                post,
                                                postLikeRepository.countByPostId(post.getId()),
                                                userGroupRepository
                                                                .findByUserIdAndGroupId(post.getUser().getId(),
                                                                                post.getGroup().getId())
                                                                .map(UserGroup::getRole).orElse(null),
                                                likedPostIds.contains(post.getId())))
                                .toList();
                return new CursorPageResponse<>(dtos, nextCursor, hasMore);
        }

        @Override
        public CursorPageResponse<PostSummaryDTO> getGroupPosts(Integer groupId, Integer userId, String cursor,
                        int size) {
                Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));

                // If the group is private, ensure the user is a member
                if ("private".equalsIgnoreCase(group.getPrivacy())) {
                        if (!userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
                                throw new org.social.common.exceptions.BusinessException(
                                                org.social.common.exceptions.ErrorCode.FORBIDDEN,
                                                "Bạn không có quyền xem bài viết của nhóm kín này");
                        }
                }

                Instant cursorInstant = (cursor != null) ? Instant.parse(cursor) : Instant.now();
                List<Post> posts = postRepository.findActivePostsByGroupIdsBefore(List.of(groupId), cursorInstant,
                                PageRequest.of(0, size + 1));

                boolean hasMore = posts.size() > size;
                List<Post> pageData = hasMore ? posts.subList(0, size) : posts;
                String nextCursor = pageData.isEmpty() ? null : pageData.getLast().getCreatedAt().toString();

                List<Integer> postIds = pageData.stream().map(Post::getId).toList();
                List<Integer> likedPostIds = (userId != null && !postIds.isEmpty())
                                ? postLikeRepository.findPostIdsByUserIdAndPostIdIn(userId, postIds)
                                : List.of();

                List<PostSummaryDTO> dtos = pageData.stream()
                                .map(post -> PostMapper.toSummaryDTO(
                                                post,
                                                postLikeRepository.countByPostId(post.getId()),
                                                userGroupRepository
                                                                .findByUserIdAndGroupId(post.getUser().getId(),
                                                                                post.getGroup().getId())
                                                                .map(UserGroup::getRole).orElse(null),
                                                likedPostIds.contains(post.getId())))
                                .toList();

                return new CursorPageResponse<>(dtos, nextCursor, hasMore);
        }

        // --- Admin Methods ---

        @Override
        public PageResponse<GroupAdminDTO> getAllGroups(int page, int size, String search) {
                Pageable pageable = PageRequest.of(page, size);
                Page<Group> groupPage;

                if (search != null && !search.trim().isEmpty()) {
                        groupPage = groupRepository.findAll(
                                        (root, query, cb) -> cb.like(cb.lower(root.get("name")),
                                                        "%" + search.toLowerCase() + "%"),
                                        pageable);
                } else {
                        groupPage = groupRepository.findAll(pageable);
                }

                List<GroupAdminDTO> dtoList = groupPage.getContent().stream().map(g -> {
                        GroupAdminDTO dto = new GroupAdminDTO();
                        dto.setId(g.getId());
                        dto.setName(g.getName());
                        dto.setPrivacy(g.getPrivacy());
                        dto.setIsActive(g.getIsActive());
                        if (g.getAdmin() != null) {
                                dto.setAdminId(g.getAdmin().getId());
                                dto.setAdminName(g.getAdmin().getUserName());
                        }
                        return dto;
                }).toList();

                return new PageResponse<>(dtoList, groupPage.getNumber(), groupPage.getSize(),
                                groupPage.getTotalElements(), groupPage.getTotalPages(),
                                groupPage.hasNext(), groupPage.hasPrevious());
        }

        @Override
        @Transactional
        public GroupAdminDTO createGroupAdmin(org.social.common.dto.admin.requests.AdminGroupCreateRequest request) {
                User admin = userRepository.findById(Long.valueOf(request.getAdminId()))
                                .orElseThrow(() -> new ResourceNotFoundException("User", request.getAdminId()));

                Group group = new Group();
                group.setName(request.getName());
                group.setPrivacy(request.getPrivacy() != null ? request.getPrivacy() : "public");
                group.setAdmin(admin);
                group.setIsActive(true);
                Group savedGroup = groupRepository.save(group);

                UserGroup membership = new UserGroup();
                UserGroupId userGroupId = new UserGroupId();
                userGroupId.setUserId(request.getAdminId());
                userGroupId.setGroupId(savedGroup.getId());
                membership.setId(userGroupId);
                membership.setUser(admin);
                membership.setGroup(savedGroup);
                membership.setRole("ADMIN");
                membership.setStatus("APPROVED");
                membership.setRequestedAt(Instant.now());
                userGroupRepository.save(membership);

                GroupAdminDTO dto = new GroupAdminDTO();
                dto.setId(savedGroup.getId());
                dto.setName(savedGroup.getName());
                dto.setPrivacy(savedGroup.getPrivacy());
                dto.setIsActive(savedGroup.getIsActive());
                dto.setAdminId(Long.valueOf(admin.getId()).intValue());
                dto.setAdminName(admin.getUserName());
                return dto;
        }

        @Override
        @Transactional
        public GroupAdminDTO updateGroupAdmin(Integer id, AdminGroupUpdateRequest request) {
                Group group = groupRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Group", id));

                group.setName(request.getName());
                if (request.getPrivacy() != null) {
                        group.setPrivacy(request.getPrivacy());
                }
                if (request.getIsActive() != null) {
                        group.setIsActive(request.getIsActive());
                }

                if (request.getAdminId() != null
                                && (group.getAdmin() == null || !group.getAdmin().getId()
                                                .equals(Long.valueOf(request.getAdminId())))) {
                        User newAdmin = userRepository.findById(Long.valueOf(request.getAdminId()))
                                        .orElseThrow(() -> new ResourceNotFoundException("User", request.getAdminId()));
                        group.setAdmin(newAdmin);

                        UserGroup membership = userGroupRepository
                                        .findByUserIdAndGroupId(request.getAdminId(), group.getId())
                                        .orElseGet(() -> {
                                                UserGroup ug = new UserGroup();
                                                UserGroupId userGroupId = new UserGroupId();
                                                userGroupId.setUserId(request.getAdminId());
                                                userGroupId.setGroupId(group.getId());
                                                ug.setId(userGroupId);
                                                ug.setUser(newAdmin);
                                                ug.setGroup(group);
                                                return ug;
                                        });
                        membership.setRole("ADMIN");
                        userGroupRepository.save(membership);
                }

                Group saved = groupRepository.save(group);

                GroupAdminDTO dto = new GroupAdminDTO();
                dto.setId(saved.getId());
                dto.setName(saved.getName());
                dto.setPrivacy(saved.getPrivacy());
                dto.setIsActive(saved.getIsActive());
                if (saved.getAdmin() != null) {
                        dto.setAdminId(saved.getAdmin().getId());
                        dto.setAdminName(saved.getAdmin().getUserName());
                }
                return dto;
        }

        @Override
        @Transactional
        public void deleteGroupAdmin(Integer id) {
                Group group = groupRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Group", id));
                group.setIsActive(false);
                groupRepository.save(group);
        }
}
