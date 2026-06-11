package org.social.userservice.services;

import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.comment.requests.CommentCreateRequest;
import org.social.common.dto.comment.requests.CommentUpdateRequest;
import org.social.common.dto.comment.views.CommentDTO;

public interface CommentService {
    CursorPageResponse<CommentDTO> getComments(Integer postId, Integer userId, String cursor, int size);
    CursorPageResponse<CommentDTO> getReplies(Integer postId, Integer commentId, Integer userId, String cursor, int size);
    CommentDTO create(Integer postId, CommentCreateRequest request);
    CommentDTO update(Integer postId, Integer commentId, CommentUpdateRequest request, Integer requestUserId);
    void delete(Integer postId, Integer commentId, Integer requestUserId);
    void like(Integer postId, Integer commentId, Integer userId);
    void unlike(Integer postId, Integer commentId, Integer userId);
}
