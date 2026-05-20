package org.social.userservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.post.mappers.PostMapper;
import org.social.common.dto.post.requests.PostCreateRequest;
import org.social.common.dto.post.requests.PostUpdateRequest;
import org.social.common.dto.post.views.PostDTO;
import org.social.common.dto.post.views.PostDetailDTO;
import org.social.common.dto.post.views.PostSummaryDTO;
import org.social.common.entities.Group;
import org.social.common.entities.Post;
import org.social.common.entities.PostDetail;
import org.social.common.entities.User;
import org.social.common.repositories.PostDetailRepository;
import org.social.common.repositories.PostRepository;
import org.social.common.repositories.UserRepository;
import org.social.userservice.services.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostDetailRepository postDetailRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PostDTO create(PostCreateRequest request) {
        User user = userRepository.findById(Long.valueOf(request.userId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user với id: " + request.userId()));

        Post post = new Post();
        post.setUser(user);
        post.setIsGroupPosted(request.isGroupPosted() != null ? request.isGroupPosted() : false);
        post.setCreatedAt(Instant.now());

        if (request.groupId() != null) {
            Group group = new Group();
            group.setId(request.groupId());
            post.setGroup(group);
        }

        Post savedPost = postRepository.save(post);

        // Tạo các PostDetail từ list contents
        List<PostDetail> details = request.contents().stream()
                .map(content -> {
                    PostDetail detail = new PostDetail();
                    detail.setPost(savedPost);
                    detail.setContent(content);
                    return detail;
                })
                .toList();

        postDetailRepository.saveAll(details);

        return PostMapper.toPostDTO(savedPost);
    }

    @Override
    public List<PostSummaryDTO> getAll() {
        return postRepository.findAllWithUser().stream()
                .map(PostMapper::toSummaryDTO)
                .toList();
    }

    @Override
    public PostDetailDTO getById(Integer id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết với id: " + id));

        List<PostDetail> details = postDetailRepository.findByPost_Id(id);
        return PostMapper.toDetailDTO(post, details);
    }

    @Override
    @Transactional
    public PostDetailDTO update(Integer id, PostUpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết với id: " + id));

        // Xóa toàn bộ PostDetail cũ, thay bằng list mới
        postDetailRepository.deleteByPost_Id(id);

        List<PostDetail> newDetails = request.contents().stream()
                .map(content -> {
                    PostDetail detail = new PostDetail();
                    detail.setPost(post);
                    detail.setContent(content);
                    return detail;
                })
                .toList();

        List<PostDetail> savedDetails = postDetailRepository.saveAll(newDetails);

        return PostMapper.toDetailDTO(post, savedDetails);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!postRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết với id: " + id);
        }
        // Xóa PostDetail trước, sau đó xóa Post
        postDetailRepository.deleteByPost_Id(id);
        postRepository.deleteById(id);
    }
}
