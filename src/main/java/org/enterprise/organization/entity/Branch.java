package org.enterprise.organization.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.TenantEntity;

@Entity
@Table(name = "branches")
@Getter @Setter
public class Branch extends TenantEntity {

    private String code;
    private String name;
    private String shortName;

    private String branchType; // SALES, FACTORY, DEPOT

    private String email;
    private String phone;

    private String country;
    private String city;
    private String zipCode;

    @Column(columnDefinition = "TEXT")
    private String address;

    private Boolean active = true;

    private Boolean headOffice = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;
}
