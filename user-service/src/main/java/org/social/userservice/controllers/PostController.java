package org.social.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.PageResponse;
import org.social.common.dto.post.requests.PostCreateRequest;
import org.social.common.dto.post.requests.PostLikeRequest;
import org.social.common.dto.post.requests.PostUpdateRequest;
import org.social.common.dto.post.views.PostDetailDTO;
import org.social.common.dto.post.views.PostDTO;
import org.social.common.dto.post.views.PostSummaryDTO;
import org.social.userservice.services.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostDTO>> create(
            @Validated @RequestBody PostCreateRequest request) {
        PostDTO dto = postService.create(request);
        return ApiResponse.created("Tạo bài viết thành công!", dto);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostSummaryDTO>>> getAll() {
        List<PostSummaryDTO> list = postService.getAll();
        return ApiResponse.ok("Lấy danh sách bài viết thành công!", list);
    }

    @GetMapping("/suggested")
    public ResponseEntity<ApiResponse<CursorPageResponse<PostSummaryDTO>>> getSuggested(
            @RequestParam Integer userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size) {
        CursorPageResponse<PostSummaryDTO> result = postService.getSuggested(userId, cursor, size);
        return ApiResponse.ok("Lấy danh sách bài viết đề xuất thành công!", result);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryDTO>>> search(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Boolean isGroupPosted,
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<PostSummaryDTO> result = postService.getFiltered(userId, isGroupPosted, groupId, keyword, page, size, sortDir);
        return ApiResponse.ok("Tìm kiếm bài viết thành công!", result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailDTO>> getById(@PathVariable Integer id) {
        PostDetailDTO dto = postService.getById(id);
        return ApiResponse.ok("Lấy chi tiết bài viết thành công!", dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailDTO>> update(
            @PathVariable Integer id,
            @Validated @RequestBody PostUpdateRequest request) {
        PostDetailDTO dto = postService.update(id, request);
        return ApiResponse.ok("Cập nhật bài viết thành công!", dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        postService.delete(id);
        return ApiResponse.deleted("Xóa bài viết thành công!");
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> like(
            @PathVariable Integer id,
            @Validated @RequestBody PostLikeRequest request) {
        postService.like(id, request.userId());
        return ApiResponse.created("Đã thích bài viết!");
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> unlike(
            @PathVariable Integer id,
            @Validated @RequestBody PostLikeRequest request) {
        postService.unlike(id, request.userId());
        return ApiResponse.deleted("Đã bỏ thích bài viết!");
    }
}
