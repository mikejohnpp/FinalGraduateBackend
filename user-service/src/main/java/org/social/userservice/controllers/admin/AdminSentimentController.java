package org.social.userservice.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.PageResponse;
import org.social.common.dto.admin.sentiment.SentimentFilterRequest;
import org.social.common.dto.admin.sentiment.SentimentItemDTO;
import org.social.common.dto.admin.sentiment.SentimentStatsDTO;
import org.social.userservice.services.AdminSentimentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/admin/sentiment")
@RequiredArgsConstructor
public class AdminSentimentController {

    private final AdminSentimentService adminSentimentService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<SentimentStatsDTO>> getOverview(
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(required = false) Double minConfidence,
            @RequestParam(required = false) Double maxConfidence,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer groupId) {
        SentimentFilterRequest filter = new SentimentFilterRequest(
                sentiment, fromDate, toDate, minConfidence, maxConfidence, keyword, groupId);
        SentimentStatsDTO result = adminSentimentService.getOverview(filter);
        return ApiResponse.ok("Lấy thống kê cảm xúc thành công", result);
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<PageResponse<SentimentItemDTO>>> getItems(
            @RequestParam(defaultValue = "post") String type,
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(required = false) Double minConfidence,
            @RequestParam(required = false) Double maxConfidence,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        SentimentFilterRequest filter = new SentimentFilterRequest(
                sentiment, fromDate, toDate, minConfidence, maxConfidence, keyword, groupId);
        PageResponse<SentimentItemDTO> result = adminSentimentService.getItems(type, filter, page, size);
        return ApiResponse.ok("Lấy danh sách nội dung theo cảm xúc thành công", result);
    }
}
