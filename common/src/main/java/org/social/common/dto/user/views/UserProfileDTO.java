package org.social.common.dto.user.views;

public record UserProfileDTO(
        Integer id,
        String userName,
        String nickName,
        String avatar,
        String email,
        Integer phoneNumber,
        String dateOfBirth,
        String role,
        Boolean isActive
) {}
