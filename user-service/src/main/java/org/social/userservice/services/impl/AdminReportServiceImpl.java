package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.admin.SystemStatsDTO;
import org.social.common.repositories.CommentRepository;
import org.social.common.repositories.GroupRepository;
import org.social.common.repositories.PostRepository;
import org.social.common.repositories.UserRepository;
import org.social.userservice.services.AdminReportService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public SystemStatsDTO getOverview() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long inactiveUsers = userRepository.countByIsActiveFalse();
        long totalGroups = groupRepository.count();
        long activeGroups = groupRepository.countByIsActiveTrue();
        long totalPosts = postRepository.countByIsActiveTrue();
        long totalComments = commentRepository.countByIsActiveTrue();

        return new SystemStatsDTO(
                totalUsers, activeUsers, inactiveUsers,
                totalGroups, activeGroups, totalPosts, totalComments);
    }

    @Override
    public String exportCsv() {
        SystemStatsDTO stats = getOverview();
        StringBuilder sb = new StringBuilder();
        sb.append("metric,value\n");
        sb.append("totalUsers,").append(stats.totalUsers()).append("\n");
        sb.append("activeUsers,").append(stats.activeUsers()).append("\n");
        sb.append("inactiveUsers,").append(stats.inactiveUsers()).append("\n");
        sb.append("totalGroups,").append(stats.totalGroups()).append("\n");
        sb.append("activeGroups,").append(stats.activeGroups()).append("\n");
        sb.append("totalPosts,").append(stats.totalPosts()).append("\n");
        sb.append("totalComments,").append(stats.totalComments()).append("\n");
        return sb.toString();
    }
}
