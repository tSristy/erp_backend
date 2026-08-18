package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "shareholder_details")
@Getter
@Setter
public class ShareholderDetail extends AuditableEntity {

    private java.math.BigDecimal equityPercentage;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    private BusinessPartner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.finance.entity.Account equityAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.finance.entity.Account dividendPayableAccount;
}
