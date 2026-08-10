package org.social.common.dto.admin.requests;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AdminContentActionRequest(
        @NotEmpty List<Integer> ids) {
}
