package org.social.common.dto.call.response;

public record CallAnswerResponse(
        Long fromUserId,
        Long toUserId,
        String sdpAnswer
) {}
