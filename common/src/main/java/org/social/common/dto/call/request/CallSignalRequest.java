package org.social.common.dto.call.request;

public record CallSignalRequest(
        String type, // OFFER, ANSWER, ICE, REJECT, HANGUP
        Object payload
) {}
