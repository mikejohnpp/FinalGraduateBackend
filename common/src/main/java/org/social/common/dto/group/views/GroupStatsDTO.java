package org.social.common.dto.group.views;

import java.util.List;

public record GroupStatsDTO(
        int pendingReviews,
        int reportedContent,
        int pendingPosts,
        int memberRequests,
        int groupStatusViolations,
        int moderationNotifications,
        int weeklyPosts,
        double weeklyPostsChange,
        int weeklyComments,
        double weeklyCommentsChange,
        int weeklyReactions,
        double weeklyReactionsChange,
        int activeMembers,
        double activeMembersChange,
        List<WeeklyActivityDTO> weeklyActivity
) {}
