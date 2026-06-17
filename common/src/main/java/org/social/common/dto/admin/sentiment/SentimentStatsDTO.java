package org.social.common.dto.admin.sentiment;

public record SentimentStatsDTO(
        long totalPosts,
        long positivePosts,
        long neutralPosts,
        long negativePosts,
        long totalComments,
        long positiveComments,
        long neutralComments,
        long negativeComments) {
}
