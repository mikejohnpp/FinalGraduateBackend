package org.social.common.dto.admin;

public record SystemStatsDTO(
        long totalUsers,
        long activeUsers,
        long inactiveUsers,
        long totalGroups,
        long activeGroups,
        long totalPosts,
        long totalComments) {
}
