package org.enterprise.organization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AreaDto {
    private Long id;
    private String code;
    private String name;
    private Long areaManagerId;
    private String areaManagerName;
    private Long zoneId;
    private String zoneName;
    private Boolean active;
}
