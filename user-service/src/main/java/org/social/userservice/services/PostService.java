package org.social.userservice.services;



import org.social.common.dto.post.requests.PostCreateRequest;
import org.social.common.dto.post.requests.PostUpdateRequest;
import org.social.common.dto.post.views.PostDTO;
import org.social.common.dto.post.views.PostDetailDTO;
import org.social.common.dto.post.views.PostSummaryDTO;

import java.util.List;

public interface PostService {

    PostDTO create(PostCreateRequest request);

    List<PostSummaryDTO> getAll();

    PostDetailDTO getById(Integer id);

    PostDetailDTO update(Integer id, PostUpdateRequest request);

    void delete(Integer id);
}
