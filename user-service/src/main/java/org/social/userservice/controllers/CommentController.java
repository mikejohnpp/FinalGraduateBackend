package org.social.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.comment.requests.CommentCreateRequest;
import org.social.common.dto.comment.requests.CommentLikeRequest;
import org.social.common.dto.comment.requests.CommentUpdateRequest;
import org.social.common.dto.comment.views.CommentDTO;
import org.social.userservice.services.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<CommentDTO>>> getComments(
            @PathVariable Integer postId,
            @RequestParam Integer userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size) {
        CursorPageResponse<CommentDTO> result = commentService.getComments(postId, userId, cursor, size);
        return ApiResponse.ok("Lấy danh sách bình luận thành công!", result);
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<CursorPageResponse<CommentDTO>>> getReplies(
            @PathVariable Integer postId,
            @PathVariable Integer commentId,
            @RequestParam Integer userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "5") int size) {
        CursorPageResponse<CommentDTO> result = commentService.getReplies(postId, commentId, userId, cursor, size);
        return ApiResponse.ok("Lấy danh sách phản hồi thành công!", result);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentDTO>> create(
            @PathVariable Integer postId,
            @Validated @RequestBody CommentCreateRequest request) {
        CommentDTO result = commentService.create(postId, request);
        return ApiResponse.ok("Tạo bình luận thành công!", result);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentDTO>> update(
            @PathVariable Integer postId,
            @PathVariable Integer commentId,
            @RequestParam Integer userId,
            @Validated @RequestBody CommentUpdateRequest request) {
        CommentDTO result = commentService.update(postId, commentId, request, userId);
        return ApiResponse.ok("Cập nhật bình luận thành công!", result);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer postId,
            @PathVariable Integer commentId,
            @RequestParam Integer userId) {
        commentService.delete(postId, commentId, userId);
        return ApiResponse.deleted("Xoá bình luận thành công!");
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> like(
            @PathVariable Integer postId,
            @PathVariable Integer commentId,
            @Validated @RequestBody CommentLikeRequest request) {
        commentService.like(postId, commentId, request.userId());
        return ApiResponse.ok("Thích bình luận thành công!");
    }

    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> unlike(
            @PathVariable Integer postId,
            @PathVariable Integer commentId,
            @Validated @RequestBody CommentLikeRequest request) {
        commentService.unlike(postId, commentId, request.userId());
        return ApiResponse.deleted("Bỏ thích bình luận thành công!");
    }
}
