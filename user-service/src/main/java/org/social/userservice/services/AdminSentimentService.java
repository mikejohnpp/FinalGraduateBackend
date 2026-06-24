package org.social.userservice.services;

import org.social.common.dto.PageResponse;
import org.social.common.dto.admin.sentiment.SentimentFilterRequest;
import org.social.common.dto.admin.sentiment.SentimentItemDTO;
import org.social.common.dto.admin.sentiment.SentimentStatsDTO;

public interface AdminSentimentService {

    SentimentStatsDTO getOverview(SentimentFilterRequest filter);

    PageResponse<SentimentItemDTO> getItems(String type, SentimentFilterRequest filter, int page, int size);
}
