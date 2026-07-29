package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.comment.mappers.CommentMapper;
import org.social.common.dto.media.MediaMapper;
import org.social.common.dto.comment.requests.CommentCreateRequest;

import org.social.common.dto.comment.requests.CommentUpdateRequest;
import org.social.common.dto.comment.views.CommentDTO;
import org.social.common.entities.Comment;
import org.social.common.entities.CommentLike;
import org.social.common.entities.NotificationType;
import org.social.common.entities.Post;
import org.social.common.entities.User;
import org.social.common.events.AnalyzeSentimentEvent;
import org.social.common.events.NotificationEvent;
import org.social.userservice.messaging.publishers.NotificationProducer;
import org.social.common.exceptions.BusinessException;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.social.common.kafka.support.EventEnvelope;
import org.social.common.kafka.support.EventPublisher;
import org.social.common.repositories.CommentLikeRepository;
import org.social.common.repositories.CommentRepository;
import org.social.common.repositories.PostRepository;
import org.social.common.repositories.UserRepository;
import org.social.userservice.services.CommentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    @Value("${app.kafka.topics.post.analyze.preprocessor}")
    private String preprocessorTopic;

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final EventPublisher userEventPublisher;
    private final NotificationProducer notificationProducer;

    @Override
    public CursorPageResponse<CommentDTO> getComments(Integer postId, Integer userId, String cursor, int size) {
        Instant cursorInstant = (cursor != null) ? Instant.parse(cursor) : Instant.now();

        List<Comment> comments = commentRepository.findActiveCommentsBefore(postId, cursorInstant,
                PageRequest.of(0, size + 1));

        boolean hasMore = comments.size() > size;
        List<Comment> pageData = hasMore ? comments.subList(0, size) : comments;

        String nextCursor = pageData.isEmpty() ? null : pageData.getLast().getCreatedAt().toString();

        List<CommentDTO> dtos = pageData.stream()
                .map(comment -> CommentMapper.toCommentDTO(comment,
                        commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), userId)))
                .toList();

        return new CursorPageResponse<>(dtos, nextCursor, hasMore);
    }

    @Override
    public CursorPageResponse<CommentDTO> getReplies(Integer postId, Integer commentId, Integer userId, String cursor,
            int size) {
        Instant cursorInstant = (cursor != null) ? Instant.parse(cursor) : Instant.EPOCH;

        List<Comment> comments = commentRepository.findActiveRepliesAfter(postId, commentId, cursorInstant,
                PageRequest.of(0, size + 1));

        boolean hasMore = comments.size() > size;
        List<Comment> pageData = hasMore ? comments.subList(0, size) : comments;

        String nextCursor = pageData.isEmpty() ? null : pageData.getLast().getCreatedAt().toString();

        List<CommentDTO> dtos = pageData.stream()
                .map(comment -> CommentMapper.toCommentDTO(comment,
                        commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), userId)))
                .toList();

        return new CursorPageResponse<>(dtos, nextCursor, hasMore);
    }

    @Override
    @Transactional
    public CommentDTO create(Integer postId, CommentCreateRequest request) {
        Post post = postRepository.findByIdAndIsActiveTrue(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", postId));

        User user = userRepository.findById(Long.valueOf(request.userId()))
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", request.userId()));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(request.content());
        comment.setIsActive(true);
        comment.setCreatedAt(Instant.now());
        comment.setLikeCount(0);
        comment.setReplyCount(0);

        if (request.parentId() != null) {
            Comment parent = commentRepository.findByIdAndIsActiveTrue(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bình luận", request.parentId()));

            if (!Objects.equals(parent.getPost().getId(), postId)) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Bình luận cha không thuộc bài viết này");
            }
            if (parent.getParent() != null) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Không thể trả lời bình luận phản hồi");
            }

            comment.setParent(parent);
            parent.setReplyCount(parent.getReplyCount() + 1);
            commentRepository.save(parent);

            // Thông báo REPLY cho chủ bình luận cha
            notificationProducer.publish(new NotificationEvent(
                    parent.getUser().getId(),
                    request.userId(),
                    NotificationType.REPLY.name(),
                    "COMMENT",
                    parent.getId(),
                    null,
                    "/posts/" + postId));
        }

        if (request.media() != null && !request.media().isEmpty()) {
            comment.getMedia().addAll(MediaMapper.toCommentMediaEntities(request.media(), comment));
        }

        int currentPostCommentCount = post.getCommentCount() != null ? post.getCommentCount() : 0;

        post.setCommentCount(currentPostCommentCount + 1);
        postRepository.save(post);

        Comment savedComment = commentRepository.save(comment);

        // Thông báo COMMENT cho chủ bài viết (chỉ với bình luận gốc)
        // entityId = id bình luận vừa tạo để FE cuộn tới đúng bình luận, link giữ
        // postId
        if (request.parentId() == null) {
            notificationProducer.publish(new NotificationEvent(
                    post.getUser().getId(),
                    request.userId(),
                    NotificationType.COMMENT.name(),
                    "COMMENT",
                    savedComment.getId(),
                    null,
                    "/posts/" + postId));
        }

        userEventPublisher.publish(
                preprocessorTopic,
                savedComment.getId().toString(),
                EventEnvelope.of("postAnalyze", "user-service",
                        new AnalyzeSentimentEvent(savedComment.getContent(), null, "COMMENT", savedComment.getId())));

        return CommentMapper.toCommentDTO(savedComment, false);
    }

    @Override
    @Transactional
    public CommentDTO update(Integer postId, Integer commentId, CommentUpdateRequest request, Integer requestUserId) {
        Comment comment = commentRepository.findByIdAndIsActiveTrue(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận", commentId));

        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw new ResourceNotFoundException("Bình luận", commentId);
        }

        if (!Objects.equals(comment.getUser().getId(), requestUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        comment.setContent(request.content());
        comment.setUpdatedAt(Instant.now());

        if (request.media() != null) {
            comment.getMedia().clear();
            comment.getMedia().addAll(MediaMapper.toCommentMediaEntities(request.media(), comment));
        }

        Comment savedComment = commentRepository.save(comment);

        boolean liked = commentLikeRepository.existsByCommentIdAndUserId(commentId, requestUserId);
        return CommentMapper.toCommentDTO(savedComment, liked);
    }

    @Override
    @Transactional
    public void delete(Integer postId, Integer commentId, Integer requestUserId) {
        Comment comment = commentRepository.findByIdAndIsActiveTrue(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận", commentId));

        Post post = comment.getPost();
        if (!Objects.equals(post.getId(), postId)) {
            throw new ResourceNotFoundException("Bình luận", commentId);
        }

        if (!Objects.equals(comment.getUser().getId(), requestUserId)
                && !Objects.equals(post.getUser().getId(), requestUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        comment.setIsActive(false);
        commentRepository.save(comment);

        int commentsRemoved = 1;

        if (comment.getParent() == null) {
            List<Comment> replies = commentRepository.findAllByParentIdAndIsActiveTrue(commentId);
            for (Comment reply : replies) {
                reply.setIsActive(false);
                commentRepository.save(reply);
                commentsRemoved++;
            }
        } else {
            Comment parent = comment.getParent();
            parent.setReplyCount(Math.max(0, parent.getReplyCount() - 1));
            commentRepository.save(parent);
        }

        int currentPostCommentCount = post.getCommentCount() != null ? post.getCommentCount() : 0;
        post.setCommentCount(Math.max(0, currentPostCommentCount - commentsRemoved));
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void like(Integer postId, Integer commentId, Integer userId) {
        Comment comment = commentRepository.findByIdAndIsActiveTrue(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận", commentId));

        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw new ResourceNotFoundException("Bình luận", commentId);
        }

        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ENTRY, "Like");
        }

        CommentLike like = new CommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        like.setCreatedAt(Instant.now());
        commentLikeRepository.save(like);

        int currentLikeCount = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
        comment.setLikeCount(currentLikeCount + 1);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void unlike(Integer postId, Integer commentId, Integer userId) {
        Comment comment = commentRepository.findByIdAndIsActiveTrue(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận", commentId));

        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw new ResourceNotFoundException("Bình luận", commentId);
        }

        if (!commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new ResourceNotFoundException("Like", commentId);
        }

        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);

        int currentLikeCount = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
        comment.setLikeCount(Math.max(0, currentLikeCount - 1));
        commentRepository.save(comment);
    }
}
