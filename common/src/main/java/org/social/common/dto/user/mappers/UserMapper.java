package org.social.common.dto.user.mappers;

import org.social.common.dto.user.views.RoleDTO;
import org.social.common.dto.user.views.UserDTO;
import org.social.common.dto.user.views.UserNoAuthenticateDTO;
import org.social.common.dto.user.views.UserWithAuthenticateDTO;
import org.social.common.entities.User;

public class UserMapper {
    public static UserWithAuthenticateDTO mapUserToUserWithAuthenticate(User user) {
        if (user == null) {
            return new UserWithAuthenticateDTO(
                    "",
                    "",
                    "Chưa active"
            );
        }

        String isActive = user.getIsActive() ? "Đã active" : "Chưa active";

        return new UserWithAuthenticateDTO(
                        user.getUserName(),
                        user.getRole().getName(),
                        isActive
                );
    }

    public static UserNoAuthenticateDTO mapUserToUserNoAuthenticate(User user) {
        return new UserNoAuthenticateDTO(
                user.getUserName(),
                user.getRole().getName()
        );
    }

    public static UserDTO mapUserToUserDTO(User user) {
        return new UserDTO(
                user.getUserName(),
                new RoleDTO(user.getRole().getName())
        );
    }
}
