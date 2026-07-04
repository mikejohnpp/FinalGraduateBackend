package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.PageResponse;
import org.social.common.dto.media.MediaMapper;
import org.social.common.dto.post.mappers.PostMapper;

import org.social.common.dto.post.requests.PostCreateRequest;
import org.social.common.dto.post.requests.PostUpdateRequest;
import org.social.common.dto.post.views.PostDTO;
import org.social.common.dto.post.views.PostDetailDTO;
import org.social.common.dto.post.views.PostSummaryDTO;
import org.social.common.entities.*;
import org.social.common.events.AnalyzeSentimentEvent;
import org.social.common.exceptions.BusinessException;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.social.common.kafka.support.EventEnvelope;
import org.social.common.kafka.support.EventPublisher;
import org.social.common.repositories.PostLikeRepository;
import org.social.common.repositories.PostRepository;
import org.social.common.repositories.UserGroupRepository;
import org.social.common.repositories.UserRepository;
import org.social.userservice.services.PostService;
import org.social.userservice.specifications.PostSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    @Value("${app.kafka.topics.post.analyze.preprocessor}")
    private String preprocessorTopic;

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final EventPublisher userEventPublisher;

    private String getAuthorRole(Post post) {
        if (Boolean.TRUE.equals(post.getIsGroupPosted()) && post.getGroup() != null) {
            return userGroupRepository.findByUserIdAndGroupId(post.getUser().getId(), post.getGroup().getId())
                    .map(UserGroup::getRole)
                    .orElse(null);
        }
        return null;
    }

    @Override
    @Transactional
    public PostDTO create(PostCreateRequest request) {
        User user = userRepository.findById(Long.valueOf(request.userId()))
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        Post post = new Post();
        post.setUser(user);
        post.setContent(request.content());
        post.setIsGroupPosted(request.isGroupPosted() != null ? request.isGroupPosted() : false);
        post.setCreatedAt(Instant.now());
        post.setIsActive(true);

        if (Boolean.TRUE.equals(request.isGroupPosted()) && request.groupId() != null) {
            UserGroup membership = userGroupRepository.findByUserIdAndGroupId(request.userId(), request.groupId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED,
                            "Bạn không phải thành viên của nhóm này"));

            Group group = new Group();
            group.setId(request.groupId());
            post.setGroup(group);

            if ("ADMIN".equals(membership.getRole()) || "OWNER".equals(membership.getRole())) {
                post.setStatus("APPROVED");
            } else {
                post.setStatus("PENDING");
            }
        } else {
            post.setStatus("APPROVED");
        }

        if (request.media() != null && !request.media().isEmpty()) {
            post.getMedia().addAll(MediaMapper.toPostMediaEntities(request.media(), post));
        }

        Post savedPost = postRepository.save(post);

        userEventPublisher.publish(
                preprocessorTopic,
                savedPost.getId().toString(),
                EventEnvelope.of("postAnalyze", "user-service",
                        new AnalyzeSentimentEvent(savedPost.getContent(), savedPost.getId(), "POST",
                                savedPost.getId())));

        return PostMapper.toPostDTO(savedPost, getAuthorRole(savedPost), false);
    }

    @Override
    public List<PostSummaryDTO> getAll(Integer userId) {
        List<Post> posts = postRepository.findAllWithUser();
        List<Integer> postIds = posts.stream().map(Post::getId).toList();
        List<Integer> likedPostIds = (userId != null && !postIds.isEmpty())
                ? postLikeRepository.findPostIdsByUserIdAndPostIdIn(userId, postIds)
                : List.of();

        return posts.stream()
                .map(post -> PostMapper.toSummaryDTO(post, postLikeRepository.countByPostId(post.getId()),
                        getAuthorRole(post), likedPostIds.contains(post.getId())))
                .toList();
    }

    @Override
    public CursorPageResponse<PostSummaryDTO> getSuggested(Integer userId, String cursor, int size) {
        Instant cursorInstant = (cursor != null) ? Instant.parse(cursor) : Instant.now();

        List<Post> posts = postRepository.findActivePostsBefore(cursorInstant, PageRequest.of(0, size + 1));

        boolean hasMore = posts.size() > size;
        List<Post> pageData = hasMore ? posts.subList(0, size) : posts;

        String nextCursor = pageData.isEmpty() ? null : pageData.getLast().getCreatedAt().toString();

        List<Integer> postIds = pageData.stream().map(Post::getId).toList();
        List<Integer> likedPostIds = (userId != null && !postIds.isEmpty())
                ? postLikeRepository.findPostIdsByUserIdAndPostIdIn(userId, postIds)
                : List.of();

        List<PostSummaryDTO> dtos = pageData.stream()
                .map(post -> PostMapper.toSummaryDTO(post, postLikeRepository.countByPostId(post.getId()),
                        getAuthorRole(post), likedPostIds.contains(post.getId())))
                .toList();

        return new CursorPageResponse<>(dtos, nextCursor, hasMore);
    }

    @Override
    public PostDetailDTO getById(Integer id, Integer userId) {
        Post post = postRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", id));
        long likeCount = postLikeRepository.countByPostId(id);
        boolean hasLiked = userId != null && postLikeRepository.existsByUserIdAndPostId(userId, id);
        return PostMapper.toDetailDTO(post, likeCount, getAuthorRole(post), hasLiked);
    }

    @Override
    @Transactional
    public PostDetailDTO update(Integer id, PostUpdateRequest request, Integer userId) {
        Post post = postRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", id));

        post.setContent(request.content());

        if (request.media() != null) {
            post.getMedia().clear();
            post.getMedia().addAll(MediaMapper.toPostMediaEntities(request.media(), post));
        }

        Post savedPost = postRepository.save(post);
        long likeCount = postLikeRepository.countByPostId(id);

        boolean hasLiked = userId != null && postLikeRepository.existsByUserIdAndPostId(userId, id);
        return PostMapper.toDetailDTO(savedPost, likeCount, getAuthorRole(savedPost), hasLiked);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Post post = postRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", id));
        post.setIsActive(false);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void like(Integer postId, Integer userId) {
        if (postRepository.findByIdAndIsActiveTrue(postId).isEmpty()) {
            throw new ResourceNotFoundException("Bài viết", postId);
        }
        if (!userRepository.existsById(Long.valueOf(userId))) {
            throw new ResourceNotFoundException("Người dùng", userId);
        }
        if (postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ENTRY, "Like");
        }
        PostLike like = new PostLike();
        like.setUserId(userId);
        like.setPostId(postId);
        like.setCreatedAt(Instant.now());
        postLikeRepository.save(like);
    }

    @Override
    @Transactional
    public void unlike(Integer postId, Integer userId) {
        if (!postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new ResourceNotFoundException("Like", postId);
        }
        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Override
    public PageResponse<PostSummaryDTO> getFiltered(Integer userId, Boolean isGroupPosted, Integer groupId,
            String keyword, int page, int size, String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Specification<Post> spec = PostSpecification.isActive()
                .and(PostSpecification.byUserId(userId))
                .and(PostSpecification.byIsGroupPosted(isGroupPosted))
                .and(PostSpecification.byGroupId(groupId))
                .and(PostSpecification.contentContains(keyword));

        Page<Post> postPage = postRepository.findAll(spec, pageable);

        List<Integer> postIds = postPage.getContent().stream().map(Post::getId).toList();
        List<Integer> likedPostIds = (userId != null && !postIds.isEmpty())
                ? postLikeRepository.findPostIdsByUserIdAndPostIdIn(userId, postIds)
                : List.of();

        List<PostSummaryDTO> dtos = postPage.getContent().stream()
                .map(post -> PostMapper.toSummaryDTO(post, postLikeRepository.countByPostId(post.getId()),
                        getAuthorRole(post), likedPostIds.contains(post.getId())))
                .toList();

        return new PageResponse<>(
                dtos,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.hasNext(),
                postPage.hasPrevious());
    }
}
