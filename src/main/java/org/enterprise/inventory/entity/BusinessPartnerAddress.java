package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "bp_addresses")
@Getter
@Setter
public class BusinessPartnerAddress extends AuditableEntity {

    private String addressType; // BILLING, SHIPPING

    private String country;
    private String city;
    private String zipCode;

    @Column(columnDefinition = "TEXT")
    private String addressLine;

    private Boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner partner;
}