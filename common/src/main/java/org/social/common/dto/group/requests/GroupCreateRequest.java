package org.social.common.dto.group.requests;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record GroupCreateRequest(
        @NotBlank(message = "Tên nhóm không được để trống") String name,
        String privacy,
        List<Integer> invitees
) {}
