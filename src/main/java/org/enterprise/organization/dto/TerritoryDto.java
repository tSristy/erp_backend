package org.enterprise.organization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TerritoryDto {
    private Long id;
    private String code;
    private String name;
    private String salesType;
    private Long territoryManagerId;
    private Boolean active;
    private Long areaId;
}
