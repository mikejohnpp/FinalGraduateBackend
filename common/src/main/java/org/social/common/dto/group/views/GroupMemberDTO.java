package org.social.common.dto.group.views;

public record GroupMemberDTO(
        Integer userId,
        String name,
        String avatar,
        String role
) {}
