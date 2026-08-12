package org.social.userservice.specifications;

import org.social.common.dto.admin.sentiment.SentimentFilterRequest;
import org.social.common.entities.Comment;
import org.springframework.data.jpa.domain.Specification;

public class CommentSentimentSpecification {

    public static Specification<Comment> build(SentimentFilterRequest filter) {
        Specification<Comment> spec = hasSentiment();

        if (filter == null) {
            return spec;
        }

        if (filter.isActive() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), filter.isActive()));
        }

        if (filter.sentiment() != null && !filter.sentiment().isBlank()) {
            spec = spec.and(bySentiment(filter.sentiment()));
        }
        if (filter.fromDate() != null) {
            spec = spec.and(createdAfter(filter));
        }
        if (filter.toDate() != null) {
            spec = spec.and(createdBefore(filter));
        }
        if (filter.minConfidence() != null) {
            spec = spec.and(minConfidence(filter));
        }
        if (filter.maxConfidence() != null) {
            spec = spec.and(maxConfidence(filter));
        }
        if (filter.keyword() != null && !filter.keyword().isBlank()) {
            spec = spec.and(contentContains(filter.keyword()));
        }
        if (filter.groupId() != null) {
            spec = spec.and(byGroupId(filter.groupId()));
        }
        return spec;
    }

    private static Specification<Comment> hasSentiment() {
        return (root, query, cb) -> cb.isNotNull(root.get("sentiment"));
    }

    private static Specification<Comment> bySentiment(String sentiment) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("sentiment")), sentiment.toLowerCase());
    }

    private static Specification<Comment> createdAfter(SentimentFilterRequest filter) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), filter.fromDate());
    }

    private static Specification<Comment> createdBefore(SentimentFilterRequest filter) {
        return (root, query, cb) -> cb.lessThan(root.get("createdAt"), filter.toDate());
    }

    private static Specification<Comment> minConfidence(SentimentFilterRequest filter) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("confidence"), filter.minConfidence());
    }

    private static Specification<Comment> maxConfidence(SentimentFilterRequest filter) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("confidence"), filter.maxConfidence());
    }

    private static Specification<Comment> contentContains(String keyword) {
        return (root, query, cb) -> cb.like(root.get("content"), "%" + keyword + "%");
    }

    private static Specification<Comment> byGroupId(Integer groupId) {
        return (root, query, cb) -> cb.equal(root.get("post").get("group").get("id"), groupId);
    }
}
