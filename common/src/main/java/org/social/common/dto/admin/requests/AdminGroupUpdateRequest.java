package org.social.common.dto.admin.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminGroupUpdateRequest {
    @NotBlank(message = "Tên nhóm không được để trống")
    private String name;

    private String privacy;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive;

    private Integer adminId;
}
