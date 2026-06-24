package org.social.common.dto.call.request;

public record IceCandidateRequest(
        Long fromUserId,
        Long toUserId,
        String candidate,
        String sdpMid,
        Integer sdpMLineIndex
) {}
