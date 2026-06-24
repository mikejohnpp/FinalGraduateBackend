package org.social.common.dto.admin;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserAdminDTO {
    private Integer id;
    private String userName;
    private String email;
    private String nickName;
    private Integer phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;
    private Boolean isActive;
    private String roleName;
}
