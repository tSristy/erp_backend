package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_landed_cost_details")
@Getter
@Setter
public class LandedCostDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private LandedCostVoucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    private CostHead costHead;

    @ManyToOne(fetch = FetchType.LAZY)
    private PurchaseInvoice relatedInvoice;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;
}
