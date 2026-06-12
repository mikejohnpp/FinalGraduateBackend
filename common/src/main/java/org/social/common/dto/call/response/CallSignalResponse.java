package org.social.common.dto.call.response;

public record CallSignalResponse(
        String type, // OFFER, ANSWER, ICE, REJECT, HANGUP
        Object payload
) {}
