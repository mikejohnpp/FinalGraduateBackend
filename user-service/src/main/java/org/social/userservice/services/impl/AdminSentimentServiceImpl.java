package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.PageResponse;
import org.social.common.dto.admin.sentiment.SentimentFilterRequest;
import org.social.common.dto.admin.sentiment.SentimentItemDTO;
import org.social.common.dto.admin.sentiment.SentimentStatsDTO;
import org.social.common.entities.Comment;
import org.social.common.entities.Group;
import org.social.common.entities.Post;
import org.social.common.entities.User;
import org.social.common.repositories.CommentRepository;
import org.social.common.repositories.PostRepository;
import org.social.userservice.services.AdminSentimentService;
import org.social.userservice.specifications.CommentSentimentSpecification;
import org.social.userservice.specifications.PostSentimentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSentimentServiceImpl implements AdminSentimentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public SentimentStatsDTO getOverview(SentimentFilterRequest filter) {
        long positivePosts = countPosts(withSentiment(filter, "positive"));
        long neutralPosts = countPosts(withSentiment(filter, "neutral"));
        long negativePosts = countPosts(withSentiment(filter, "negative"));
        long totalPosts = positivePosts + neutralPosts + negativePosts;

        long positiveComments = countComments(withSentiment(filter, "positive"));
        long neutralComments = countComments(withSentiment(filter, "neutral"));
        long negativeComments = countComments(withSentiment(filter, "negative"));
        long totalComments = positiveComments + neutralComments + negativeComments;

        return new SentimentStatsDTO(
                totalPosts, positivePosts, neutralPosts, negativePosts,
                totalComments, positiveComments, neutralComments, negativeComments);
    }

    @Override
    public PageResponse<SentimentItemDTO> getItems(String type, SentimentFilterRequest filter, int page, int size) {
        if ("comment".equalsIgnoreCase(type)) {
            return getCommentItems(filter, page, size);
        }
        return getPostItems(filter, page, size);
    }

    private PageResponse<SentimentItemDTO> getPostItems(SentimentFilterRequest filter, int page, int size) {
        Specification<Post> spec = PostSentimentSpecification.build(filter);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> result = postRepository.findAll(spec, pageable);

        List<SentimentItemDTO> dtos = result.getContent().stream()
                .map(this::toItemDTO)
                .toList();

        return new PageResponse<>(dtos, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(),
                result.hasNext(), result.hasPrevious());
    }

    private PageResponse<SentimentItemDTO> getCommentItems(SentimentFilterRequest filter, int page, int size) {
        Specification<Comment> spec = CommentSentimentSpecification.build(filter);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> result = commentRepository.findAll(spec, pageable);

        List<SentimentItemDTO> dtos = result.getContent().stream()
                .map(this::toItemDTO)
                .toList();

        return new PageResponse<>(dtos, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(),
                result.hasNext(), result.hasPrevious());
    }

    private long countPosts(SentimentFilterRequest filter) {
        return postRepository.count(PostSentimentSpecification.build(filter));
    }

    private long countComments(SentimentFilterRequest filter) {
        return commentRepository.count(CommentSentimentSpecification.build(filter));
    }

    private SentimentFilterRequest withSentiment(SentimentFilterRequest filter, String sentiment) {
        if (filter == null) {
            return new SentimentFilterRequest(sentiment, null, null, null, null, null, null, null);
        }
        return new SentimentFilterRequest(sentiment, filter.fromDate(), filter.toDate(),
                filter.minConfidence(), filter.maxConfidence(), filter.keyword(), filter.groupId(), filter.isActive());
    }

    private SentimentItemDTO toItemDTO(Post post) {
        User author = post.getUser();
        Group group = post.getGroup();
        return new SentimentItemDTO(
                "POST",
                post.getId(),
                post.getContent(),
                post.getSentiment(),
                post.getConfidence(),
                author != null ? author.getId() : null,
                author != null ? author.getUserName() : null,
                group != null ? group.getId() : null,
                group != null ? group.getName() : null,
                post.getCreatedAt(),
                post.getIsActive());
    }

    private SentimentItemDTO toItemDTO(Comment comment) {
        User author = comment.getUser();
        Group group = comment.getPost() != null ? comment.getPost().getGroup() : null;
        return new SentimentItemDTO(
                "COMMENT",
                comment.getId(),
                comment.getContent(),
                comment.getSentiment(),
                comment.getConfidence(),
                author != null ? author.getId() : null,
                author != null ? author.getUserName() : null,
                group != null ? group.getId() : null,
                group != null ? group.getName() : null,
                comment.getCreatedAt(),
                comment.getIsActive());
    }
}
