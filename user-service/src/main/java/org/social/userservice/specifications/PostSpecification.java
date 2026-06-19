package org.social.userservice.specifications;

import org.social.common.entities.Post;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecification {

    public static Specification<Post> isActive() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("isActive")),
                cb.or(
                        cb.equal(root.get("status"), "APPROVED"),
                        cb.isNull(root.get("status"))
                )
        );
    }

    public static Specification<Post> byUserId(Integer userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Post> byIsGroupPosted(Boolean isGroupPosted) {
        return (root, query, cb) ->
                isGroupPosted == null ? null : cb.equal(root.get("isGroupPosted"), isGroupPosted);
    }

    public static Specification<Post> byGroupId(Integer groupId) {
        return (root, query, cb) ->
                groupId == null ? null : cb.equal(root.get("group").get("id"), groupId);
    }

    public static Specification<Post> contentContains(String keyword) {
        return (root, query, cb) ->
                (keyword == null || keyword.isBlank()) ? null
                        : cb.like(cb.lower(root.get("content")), "%" + keyword.toLowerCase() + "%");
    }
}
