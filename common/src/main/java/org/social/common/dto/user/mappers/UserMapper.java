package org.social.common.dto.user.mappers;

import org.social.common.dto.user.views.UserProfileDTO;
import org.social.common.entities.User;

public class UserMapper {

    public static UserProfileDTO mapUserToProfile(User user, int friendCount) {
        return new UserProfileDTO(
                user.getId(),
                user.getUserName(),
                user.getNickName(),
                user.getAvatar(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null,
                user.getRole() != null ? user.getRole().getName() : null,
                user.getIsActive(),
                user.getCoverPhoto(),
                friendCount,
                user.getBio(),
                user.getLocation(),
                user.getEducation(),
                user.getWorkplace(),
                user.getHometown(),
                user.getRelationship(),
                user.getGender(),
                user.getPronouns(),
                user.getLanguage()
        );
    }
}
