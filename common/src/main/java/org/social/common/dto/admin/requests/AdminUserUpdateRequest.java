package org.social.common.dto.admin.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AdminUserUpdateRequest {
    @NotBlank(message = "Username không được để trống")
    private String userName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotNull(message = "Role ID không được để trống")
    private Integer roleId;

    private String nickName;
    private Integer phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;
    private Boolean isActive;
}
