package org.social.common.dto.call.request;

import org.social.common.dto.call.CallType;

public record CallOfferRequest(
        Long fromUserId,
        Long toUserId,
        String sdpOffer,
        CallType callType
) {}
