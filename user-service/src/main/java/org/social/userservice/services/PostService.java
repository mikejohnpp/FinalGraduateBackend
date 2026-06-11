package org.social.userservice.services;

import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.PageResponse;
import org.social.common.dto.post.requests.PostCreateRequest;
import org.social.common.dto.post.requests.PostUpdateRequest;
import org.social.common.dto.post.views.PostDTO;
import org.social.common.dto.post.views.PostDetailDTO;
import org.social.common.dto.post.views.PostSummaryDTO;

import java.util.List;

public interface PostService {

    PostDTO create(PostCreateRequest request);

    List<PostSummaryDTO> getAll(Integer userId);

    CursorPageResponse<PostSummaryDTO> getSuggested(Integer userId, String cursor, int size);

    PostDetailDTO getById(Integer id, Integer userId);

    PostDetailDTO update(Integer id, PostUpdateRequest request, Integer userId);

    void delete(Integer id);

    void like(Integer postId, Integer userId);

    void unlike(Integer postId, Integer userId);

    PageResponse<PostSummaryDTO> getFiltered(Integer userId, Boolean isGroupPosted, Integer groupId, String keyword, int page, int size, String sortDir);
}

