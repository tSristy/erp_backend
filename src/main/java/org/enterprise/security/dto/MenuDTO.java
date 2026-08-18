package org.enterprise.security.dto;

import lombok.Data;

import lombok.EqualsAndHashCode;
import org.enterprise.common.dto.AuditableDto;

@Data
@EqualsAndHashCode(callSuper = true)
public class MenuDTO extends AuditableDto {
    private String code;
    private String name;
    private String path;
    private String icon;
    private Integer displayOrder;
    private Long parentId;
    private String parent;
    private Long moduleId;
    private String module;
    private Boolean visible;
    private Boolean isReportMenu;
}
