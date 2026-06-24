package org.social.common.dto.call.response;

public record IceCandidateResponse(
        Long fromUserId,
        Long toUserId,
        String candidate,
        String sdpMid,
        Integer sdpMLineIndex
) {}
