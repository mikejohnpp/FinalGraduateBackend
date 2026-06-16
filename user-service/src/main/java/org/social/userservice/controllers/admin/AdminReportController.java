package org.social.userservice.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.social.common.dto.ApiResponse;
import org.social.common.dto.admin.SystemStatsDTO;
import org.social.userservice.services.AdminReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<SystemStatsDTO>> getOverview() {
        SystemStatsDTO result = adminReportService.getOverview();
        return ApiResponse.ok("Lấy thống kê hệ thống thành công", result);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = adminReportService.exportCsv();
        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        byte[] content = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, content, 0, bom.length);
        System.arraycopy(body, 0, content, bom.length, body.length);

        String fileName = "system-report-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(content);
    }
}
