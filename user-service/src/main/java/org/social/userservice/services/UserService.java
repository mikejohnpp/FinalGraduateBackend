package org.social.userservice.services;

import org.social.common.dto.user.views.UserDTO;
import org.social.common.dto.user.views.UserNoAuthenticateDTO;
import org.social.common.dto.user.views.UserWithAuthenticateDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAll();
    UserWithAuthenticateDTO getUserWithAuthenticate(long id);
    List<UserWithAuthenticateDTO> getListUserWithAuthenticate();
}
