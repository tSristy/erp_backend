package org.enterprise.organization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ZoneDto {
    private Long id;
    private String code;
    private String name;
    private Long regionalManagerId;
    private Boolean active;
}
