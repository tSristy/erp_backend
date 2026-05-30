package org.enterprise.organization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BranchDto {
    private Long id;
    private String code;
    private String name;
    private String shortName;
    private String branchType;
    private String email;
    private String phone;
    private String country;
    private String city;
    private String zipCode;
    private String address;
    private Boolean active;
    private Boolean headOffice;
    private Long companyId;
}
