package org.enterprise.security.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PermissionDto {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String code;
    private String name;
    private String moduleCode;
    private String actionPath;
    private String actionType;
    private String description;
    private Long menuId;
    private String menuCode;
}
