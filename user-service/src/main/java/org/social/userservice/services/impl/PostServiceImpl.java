package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.PageResponse;
import org.social.common.dto.post.mappers.PostMapper;
import org.social.common.dto.post.requests.PostCreateRequest;
import org.social.common.dto.post.requests.PostUpdateRequest;
import org.social.common.dto.post.views.PostDTO;
import org.social.common.dto.post.views.PostDetailDTO;
import org.social.common.dto.post.views.PostSummaryDTO;
import org.social.common.entities.Group;
import org.social.common.entities.Post;
import org.social.common.entities.PostLike;
import org.social.common.entities.User;
import org.social.common.exceptions.BusinessException;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ResourceNotFoundException;
import org.social.common.repositories.PostLikeRepository;
import org.social.common.repositories.PostRepository;
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

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

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

        if (request.groupId() != null) {
            Group group = new Group();
            group.setId(request.groupId());
            post.setGroup(group);
        }

        Post savedPost = postRepository.save(post);
        return PostMapper.toPostDTO(savedPost);
    }

    @Override
    public List<PostSummaryDTO> getAll() {
        return postRepository.findAllWithUser().stream()
                .map(post -> PostMapper.toSummaryDTO(post, postLikeRepository.countByPostId(post.getId())))
                .toList();
    }

    @Override
    public CursorPageResponse<PostSummaryDTO> getSuggested(Integer userId, String cursor, int size) {
        // TODO: Trong tương lai sẽ implement thuật toán lấy bài viết tương ứng với sự quan tâm của user
        Instant cursorInstant = (cursor != null) ? Instant.parse(cursor) : Instant.now();

        // Lấy size+1 để phát hiện xem còn bài viết tiếp theo không
        List<Post> posts = postRepository.findActivePostsBefore(cursorInstant, PageRequest.of(0, size + 1));

        boolean hasMore = posts.size() > size;
        List<Post> pageData = hasMore ? posts.subList(0, size) : posts;

        String nextCursor = pageData.isEmpty() ? null : pageData.getLast().getCreatedAt().toString();

        List<PostSummaryDTO> dtos = pageData.stream()
                .map(post -> PostMapper.toSummaryDTO(post, postLikeRepository.countByPostId(post.getId())))
                .toList();

        return new CursorPageResponse<>(dtos, nextCursor, hasMore);
    }

    @Override
    public PostDetailDTO getById(Integer id) {
        Post post = postRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", id));
        long likeCount = postLikeRepository.countByPostId(id);
        return PostMapper.toDetailDTO(post, likeCount);
    }

    @Override
    @Transactional
    public PostDetailDTO update(Integer id, PostUpdateRequest request) {
        Post post = postRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết", id));

        post.setContent(request.content());
        Post savedPost = postRepository.save(post);
        long likeCount = postLikeRepository.countByPostId(id);
        return PostMapper.toDetailDTO(savedPost, likeCount);
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
    public PageResponse<PostSummaryDTO> getFiltered(Integer userId, Boolean isGroupPosted, Integer groupId, String keyword, int page, int size, String sortDir) {
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

        List<PostSummaryDTO> dtos = postPage.getContent().stream()
                .map(post -> PostMapper.toSummaryDTO(post, postLikeRepository.countByPostId(post.getId())))
                .toList();

        return new PageResponse<>(
                dtos,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.hasNext(),
                postPage.hasPrevious()
        );
    }
}
