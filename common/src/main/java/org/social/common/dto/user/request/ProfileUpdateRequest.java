package org.social.common.dto.user.request;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
                @Size(max = 150) String userName,
                @Size(max = 150) String nickName,
                @Size(max = 101) String bio,

                @Size(max = 100) String location,
                @Size(max = 200) String education,
                @Size(max = 200) String workplace,
                @Size(max = 100) String hometown,
                String dateOfBirth,
                @Size(max = 50) String relationship,
                @Size(max = 20) String gender,
                @Size(max = 50) String pronouns,
                @Size(max = 50) String language,
                @Size(max = 1000) String avatar,
                @Size(max = 1000) String coverPhoto) {
}
