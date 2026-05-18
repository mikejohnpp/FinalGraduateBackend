package org.social.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.post.requests.PostCreateRequest;
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
        return ApiResponse.ok("Tạo bài viết thành công!", dto);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostSummaryDTO>>> getAll() {
        List<PostSummaryDTO> list = postService.getAll();
        return ApiResponse.ok("Lấy danh sách bài viết thành công!", list);
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
        return ApiResponse.ok("Xóa bài viết thành công!");
    }
}
