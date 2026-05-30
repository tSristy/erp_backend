package org.enterprise.organization.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class CompanyDto {
    private Long id;
    private String code;
    private String name;
    private String shortName;
    private String email;
    private String phone;
    private String mobile;
    private String website;
    private String country;
    private String division;
    private String district;
    private String city;
    private String zipCode;
    private String address;
    private String taxNumber;
    private String vatNumber;
    private String tradeLicenseNo;
    private String currencyCode;
    private String timezone;
    private String languageCode;
    private String logoUrl;
    private Boolean active;
    private LocalDate startDate;
    private LocalDate endDate;
}
