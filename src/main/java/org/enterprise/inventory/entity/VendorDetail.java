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

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    private BusinessPartner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.finance.entity.Account accountsPayableAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.finance.entity.Account grnClearingAccount;

    private Integer paymentTermDays;
}