package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "bp_financials")
@Getter
@Setter
public class BusinessPartnerFinancial extends AuditableEntity {

    private Double creditLimit;

    private Integer paymentTermDays;

    private String currency;

    private String taxNumber;

    private String vatNumber;

    private String bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner partner;
}