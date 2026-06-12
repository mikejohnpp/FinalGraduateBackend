package org.social.common.dto.call.response;

import org.social.common.dto.call.CallType;

public record CallOfferResponse(
        Long fromUserId,
        Long toUserId,
        String sdpOffer,
        CallType callType
) {}
