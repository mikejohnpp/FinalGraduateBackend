package org.social.common.dto.search;

import java.util.List;

public record SearchResultDTO(
        List<UserSearchDTO> users,
        List<GroupSearchDTO> groups
) {
    public record UserSearchDTO(
            Long id,
            String name,
            String nickName,
            String avatar
    ) {}

    public record GroupSearchDTO(
            Integer id,
            String name,
            String avatar,
            Integer memberCount
    ) {}
}
