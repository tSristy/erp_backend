package org.enterprise.security.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.enterprise.common.dto.AuditableDto;

@Data
@EqualsAndHashCode(callSuper = true)
public class ModuleDto extends AuditableDto {
    private String code;
    private String name;
    private String icon;
    private String route;
    private Integer displayOrder;
    private Boolean active;
    private Boolean installed;
    private Boolean visibleInLauncher;
    private String category;
}
