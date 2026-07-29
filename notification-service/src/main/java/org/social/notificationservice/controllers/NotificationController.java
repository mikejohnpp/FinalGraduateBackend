package org.social.notificationservice.controllers;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.CursorPageResponse;
import org.social.common.dto.notification.views.NotificationDTO;
import org.social.notificationservice.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** GET /notifications?userId={}&unreadOnly={}&cursor={}&size={} */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<NotificationDTO>>> getNotifications(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "15") int size) {
        CursorPageResponse<NotificationDTO> result = notificationService.getNotifications(userId, unreadOnly, cursor,
                size);
        return ApiResponse.ok("Lấy danh sách thông báo thành công!", result);
    }

    /** GET /notifications/unread-count?userId={} */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@RequestParam Integer userId) {
        long count = notificationService.countUnread(userId);
        return ApiResponse.ok("Lấy số thông báo chưa đọc thành công!", count);
    }

    /** PUT /notifications/{id}/read?userId={} */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @RequestParam Integer userId) {
        notificationService.markAsRead(id, userId);
        return ApiResponse.ok("Đã đánh dấu thông báo là đã đọc!");
    }

    /** PUT /notifications/read-all?userId={} */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@RequestParam Integer userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.ok("Đã đánh dấu tất cả thông báo là đã đọc!");
    }
}
