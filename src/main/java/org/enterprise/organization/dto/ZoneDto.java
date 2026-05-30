package org.enterprise.organization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ZoneDto {
    private Long id;
    private String code;
    private String name;
    private String managerName;
    private String contactNo;
    private Boolean active;
}
