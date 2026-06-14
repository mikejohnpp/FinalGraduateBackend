package org.social.common.dto.admin.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminGroupCreateRequest {
    @NotBlank(message = "Tên nhóm không được để trống")
    private String name;

    private String privacy;

    @NotNull(message = "Admin ID không được để trống")
    private Integer adminId;
}
