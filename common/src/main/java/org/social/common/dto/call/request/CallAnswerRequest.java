package org.social.common.dto.call.request;

public record CallAnswerRequest(
        Long fromUserId,
        Long toUserId,
        String sdpAnswer
) {}
