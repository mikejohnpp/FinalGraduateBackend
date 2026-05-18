package org.social.common.dto.user.views;

public record UserWithAuthenticateDTO(
        String name,
        String role,
        String isActive
) {
}
