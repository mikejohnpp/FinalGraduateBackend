package org.social.userservice.services;

import org.social.common.dto.user.views.UserProfileDTO;

public interface UserService {
    UserProfileDTO getUserProfile(long id, String requestingEmail);
}
