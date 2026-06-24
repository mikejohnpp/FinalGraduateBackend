package org.social.userservice.services;

import org.social.common.dto.admin.SystemStatsDTO;

public interface AdminReportService {

    SystemStatsDTO getOverview();

    String exportCsv();
}
