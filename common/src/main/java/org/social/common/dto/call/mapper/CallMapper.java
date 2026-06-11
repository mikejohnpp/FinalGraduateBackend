package org.social.common.dto.call.mapper;

import org.social.common.dto.call.request.CallSignalRequest;
import org.social.common.dto.call.response.CallSignalResponse;

public class CallMapper {
    public static CallSignalResponse toResponseDTO(CallSignalRequest request) {
        return new CallSignalResponse(request.type(), request.payload());
    }
}
