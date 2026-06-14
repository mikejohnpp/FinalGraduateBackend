package org.social.common.dto.admin;

import lombok.Data;

@Data
public class GroupAdminDTO {
    private Integer id;
    private String name;
    private String privacy;
    private Boolean isActive;
    private Integer adminId;
    private String adminName;
}
