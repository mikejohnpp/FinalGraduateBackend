package org.social.userservice.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.admin.requests.AdminContentActionRequest;
import org.social.userservice.services.AdminContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/content")
@RequiredArgsConstructor
public class AdminContentController {

    private final AdminContentService adminContentService;

    @PostMapping("/posts/lock")
    public ResponseEntity<ApiResponse<Void>> lockPosts(
            @Validated @RequestBody AdminContentActionRequest request) {
        adminContentService.lockPosts(request.ids());
        return ApiResponse.ok("Đã khóa " + request.ids().size() + " bài viết");
    }

    @PostMapping("/posts/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockPosts(
            @Validated @RequestBody AdminContentActionRequest request) {
        adminContentService.unlockPosts(request.ids());
        return ApiResponse.ok("Đã mở khóa " + request.ids().size() + " bài viết");
    }

    @PostMapping("/comments/lock")
    public ResponseEntity<ApiResponse<Void>> lockComments(
            @Validated @RequestBody AdminContentActionRequest request) {
        adminContentService.lockComments(request.ids());
        return ApiResponse.ok("Đã khóa " + request.ids().size() + " bình luận");
    }

    @PostMapping("/comments/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockComments(
            @Validated @RequestBody AdminContentActionRequest request) {
        adminContentService.unlockComments(request.ids());
        return ApiResponse.ok("Đã mở khóa " + request.ids().size() + " bình luận");
    }
}
