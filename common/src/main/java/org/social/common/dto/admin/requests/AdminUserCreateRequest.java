package org.social.common.dto.admin.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserCreateRequest {
    @NotBlank(message = "Username không được để trống")
    private String userName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @NotNull(message = "Role ID không được để trống")
    private Integer roleId;

    private String nickName;
}
