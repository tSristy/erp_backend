package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "customer_details")
@Getter
@Setter
public class CustomerDetail extends AuditableEntity {

    private String customerGroup;

    private String priceList;

    private Boolean allowPartialDelivery = true;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    private BusinessPartner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.organization.entity.Territory territory;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.finance.entity.Account accountsReceivableAccount;

    private java.math.BigDecimal creditLimit;

    private Integer paymentTermDays;
}