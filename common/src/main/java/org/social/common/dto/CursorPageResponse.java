package org.social.common.dto;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> data,
        String nextCursor,
        boolean hasMore
) {}
