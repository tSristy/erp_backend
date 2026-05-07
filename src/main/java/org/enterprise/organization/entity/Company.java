package org.enterprise.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "companies")
@Getter @Setter
public class Company extends BaseEntity {

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

    @Column(columnDefinition = "TEXT")
    private String address;

    private String taxNumber;
    private String vatNumber;
    private String tradeLicenseNo;

    private String currencyCode;
    private String timezone;
    private String languageCode;

    private String logoUrl;

    private Boolean active = true;

    private LocalDate startDate;
    private LocalDate endDate;
}