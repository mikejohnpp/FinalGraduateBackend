package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.PageResponse;
import org.social.common.dto.group.mappers.GroupAdminMapper;
import org.social.common.dto.group.views.*;
import org.social.common.entities.Group;
import org.social.common.entities.NotificationType;
import org.social.common.entities.Post;
import org.social.common.entities.UserGroup;
import org.social.common.events.NotificationEvent;
import org.social.common.exceptions.BusinessException;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ResourceNotFoundException;
import org.social.common.repositories.*;
import org.social.userservice.messaging.publishers.NotificationProducer;
import org.social.userservice.services.GroupAdminService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GroupAdminServiceImpl implements GroupAdminService {

    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final NotificationProducer notificationProducer;

    private Group findActiveGroup(Integer groupId) {

        return groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
    }

    private void verifyAdmin(Integer groupId, Integer userId) {
        UserGroup membership = userGroupRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền quản trị nhóm này"));
        if (!"ADMIN".equals(membership.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền quản trị nhóm này");
        }
    }

    @Override
    public GroupAdminInfoDTO getGroupInfo(Integer groupId, Integer userId) {
        Group group = findActiveGroup(groupId);
        verifyAdmin(groupId, userId);

        long memberCount = userGroupRepository.countByGroupIdAndStatus(groupId, "APPROVED");
        UserGroup membership = userGroupRepository.findByUserIdAndGroupId(userId, groupId).orElse(null);
        String role = membership != null ? membership.getRole() : null;

        return GroupAdminMapper.toAdminInfoDTO(group, memberCount, role);
    }

    @Override
    public GroupStatsDTO getGroupStats(Integer groupId, Integer userId) {
        findActiveGroup(groupId);
        verifyAdmin(groupId, userId);

        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(Duration.ofDays(7));
        Instant fourteenDaysAgo = now.minus(Duration.ofDays(14));

        int pendingPosts = (int) postRepository.countByGroupIdAndStatusAndIsActiveTrue(groupId, "PENDING");
        int memberRequests = (int) userGroupRepository.countByGroupIdAndStatus(groupId, "PENDING");
        int pendingReviews = pendingPosts + memberRequests;

        long weeklyPostsCurrent = postRepository.countByGroupIdAndCreatedAtAfter(groupId, sevenDaysAgo);
        long weeklyPostsPrevious = postRepository.countByGroupIdAndCreatedAtBetween(groupId, fourteenDaysAgo,
                sevenDaysAgo);
        double weeklyPostsChange = calculatePercentChange(weeklyPostsCurrent, weeklyPostsPrevious);

        long weeklyCommentsCurrent = commentRepository.countByGroupIdAndCreatedAtAfter(groupId, sevenDaysAgo);
        long weeklyCommentsPrevious = commentRepository.countByGroupIdAndCreatedAtAfter(groupId, fourteenDaysAgo)
                - weeklyCommentsCurrent;
        double weeklyCommentsChange = calculatePercentChange(weeklyCommentsCurrent,
                Math.max(weeklyCommentsPrevious, 0));

        long weeklyReactionsCurrent = postLikeRepository.countByGroupIdAndCreatedAtAfter(groupId, sevenDaysAgo);
        long weeklyReactionsPrevious = postLikeRepository.countByGroupIdAndCreatedAtAfter(groupId, fourteenDaysAgo)
                - weeklyReactionsCurrent;
        double weeklyReactionsChange = calculatePercentChange(weeklyReactionsCurrent,
                Math.max(weeklyReactionsPrevious, 0));

        int activeMembers = 0;
        double activeMembersChange = 0;

        List<WeeklyActivityDTO> weeklyActivity = buildWeeklyActivity(groupId, now);

        return new GroupStatsDTO(
                pendingReviews, 0, pendingPosts, memberRequests, 0, 0,
                (int) weeklyPostsCurrent, weeklyPostsChange,
                (int) weeklyCommentsCurrent, weeklyCommentsChange,
                (int) weeklyReactionsCurrent, weeklyReactionsChange,
                activeMembers, activeMembersChange,
                weeklyActivity);
    }

    @Override
    public PageResponse<MemberRequestDTO> getMemberRequests(Integer groupId, Integer userId,
            String search, String gender, String sort,
            int page, int size) {
        findActiveGroup(groupId);
        verifyAdmin(groupId, userId);

        Sort sortOrder = "oldest".equals(sort)
                ? Sort.by(Sort.Direction.ASC, "requestedAt")
                : Sort.by(Sort.Direction.DESC, "requestedAt");
        PageRequest pageable = PageRequest.of(page, size, sortOrder);

        Page<UserGroup> resultPage;
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasGender = gender != null && !gender.trim().isEmpty() && !"ALL".equalsIgnoreCase(gender);

        if (hasSearch && hasGender) {
            resultPage = userGroupRepository.findByGroupIdAndStatusAndSearchAndGender(groupId, "PENDING", search,
                    gender, pageable);
        } else if (hasSearch) {
            resultPage = userGroupRepository.findByGroupIdAndStatusAndSearch(groupId, "PENDING", search, pageable);
        } else if (hasGender) {
            resultPage = userGroupRepository.findByGroupIdAndStatusAndGender(groupId, "PENDING", gender, pageable);
        } else {
            resultPage = userGroupRepository.findByGroupIdAndStatus(groupId, "PENDING", pageable);
        }

        List<MemberRequestDTO> dtos = resultPage.getContent().stream()
                .map(GroupAdminMapper::toMemberRequestDTO)
                .toList();

        return new PageResponse<>(dtos, resultPage.getNumber(), resultPage.getSize(),
                resultPage.getTotalElements(), resultPage.getTotalPages(),
                resultPage.hasNext(), resultPage.hasPrevious());
    }

    @Override
    @Transactional
    public void approveMemberRequests(Integer groupId, Integer userId, List<Integer> requestIds) {
        findActiveGroup(groupId);
        verifyAdmin(groupId, userId);

        List<UserGroup> requests = userGroupRepository.findByGroupIdAndUserIdsAndStatus(groupId, requestIds, "PENDING");
        if (requests.isEmpty()) {
            throw new BusinessException("Không tìm thấy yêu cầu hợp lệ");
        }

        for (UserGroup ug : requests) {
            ug.setStatus("APPROVED");
            ug.setRole("MEMBER");
        }
        userGroupRepository.saveAll(requests);

        // Thông báo GROUP_JOIN_APPROVED cho từng thành viên được duyệt
        for (UserGroup ug : requests) {
            notificationProducer.publish(new NotificationEvent(
                    ug.getUser().getId(),
                    userId,
                    NotificationType.GROUP_JOIN_APPROVED.name(),
                    "GROUP",
                    groupId,
                    null,
                    "/groups/" + groupId));
        }
    }

    @Override
    @Transactional
    public void rejectMemberRequests(Integer groupId, Integer userId, List<Integer> requestIds) {
        findActiveGroup(groupId);
        verifyAdmin(groupId, userId);

        List<UserGroup> requests = userGroupRepository.findByGroupIdAndUserIdsAndStatus(groupId, requestIds, "PENDING");
        if (requests.isEmpty()) {
            throw new BusinessException("Không tìm thấy yêu cầu hợp lệ");
        }

        for (UserGroup ug : requests) {
            ug.setStatus("REJECTED");
        }
        userGroupRepository.saveAll(requests);
    }

    @Override
    public PageResponse<PendingPostDTO> getPendingPosts(Integer groupId, Integer userId, int page, int size) {
        findActiveGroup(groupId);
        verifyAdmin(groupId, userId);

        PageRequest pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByGroupIdAndStatusAndIsActiveTrue(groupId, "PENDING", pageable);

        List<PendingPostDTO> dtos = postPage.getContent().stream()
                .map(GroupAdminMapper::toPendingPostDTO)
                .toList();

        return new PageResponse<>(dtos, postPage.getNumber(), postPage.getSize(),
                postPage.getTotalElements(), postPage.getTotalPages(),
                postPage.hasNext(), postPage.hasPrevious());
    }

    @Override
    @Transactional
    public void approvePost(Integer groupId, Integer userId, Integer postId) {
        findActiveGroup(groupId);
        verifyAdmin(groupId, userId);

        Post post = postRepository.findByIdAndIsActiveTrue(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        if (post.getGroup() == null || !post.getGroup().getId().equals(groupId)) {
            throw new BusinessException("Bài viết không thuộc nhóm này");
        }
        if (!"PENDING".equals(post.getStatus())) {
            throw new BusinessException("Bài viết không ở trạng thái chờ duyệt");
        }

        post.setStatus("APPROVED");
        postRepository.save(post);

        // Thông báo GROUP_POST_APPROVED cho tác giả bài viết
        if (post.getUser() != null) {
            notificationProducer.publish(new NotificationEvent(
                    post.getUser().getId(),
                    userId,
                    NotificationType.GROUP_POST_APPROVED.name(),
                    "POST",
                    postId,
                    null,
                    "/groups/" + groupId));
        }
    }

    @Override
    @Transactional
    public void rejectPost(Integer groupId, Integer userId, Integer postId) {
        findActiveGroup(groupId);
        verifyAdmin(groupId, userId);

        Post post = postRepository.findByIdAndIsActiveTrue(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        if (post.getGroup() == null || !post.getGroup().getId().equals(groupId)) {
            throw new BusinessException("Bài viết không thuộc nhóm này");
        }
        if (!"PENDING".equals(post.getStatus())) {
            throw new BusinessException("Bài viết không ở trạng thái chờ duyệt");
        }

        post.setStatus("REJECTED");
        postRepository.save(post);
    }

    private double calculatePercentChange(long current, long previous) {
        if (previous == 0)
            return 0;
        return Math.round(((double) (current - previous) / previous) * 1000.0) / 10.0;
    }

    private List<WeeklyActivityDTO> buildWeeklyActivity(Integer groupId, Instant now) {
        List<WeeklyActivityDTO> activity = new ArrayList<>();
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate today = now.atZone(zone).toLocalDate();

        String[] dayLabels = { "CN", "T2", "T3", "T4", "T5", "T6", "T7" };

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Instant dayStart = date.atStartOfDay(zone).toInstant();
            Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();

            long posts = postRepository.countByGroupIdAndCreatedAtBetween(groupId, dayStart, dayEnd);
            int dayOfWeek = date.getDayOfWeek().getValue();
            String label = dayLabels[dayOfWeek % 7];

            activity.add(new WeeklyActivityDTO(label, (int) posts));
        }

        return activity;
    }
}
