package org.enterprise.security.dto;

import lombok.Data;

@Data
public class RoleDto {
    private Long id;
    private String code;
    private String name;
    private Long companyId;
}
