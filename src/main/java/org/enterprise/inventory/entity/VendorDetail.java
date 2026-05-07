package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "vendor_details")
@Getter
@Setter
public class VendorDetail extends AuditableEntity {

    private Integer leadTimeDays;

    private String supplierType;

    private Boolean preferredVendor = false;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner partner;
}