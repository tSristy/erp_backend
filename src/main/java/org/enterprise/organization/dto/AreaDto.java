package org.enterprise.organization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AreaDto {
    private Long id;
    private String code;
    private String name;
    private String marketType;
    private Integer priorityLevel;
    private Boolean active;
    private Long territoryId;
}
