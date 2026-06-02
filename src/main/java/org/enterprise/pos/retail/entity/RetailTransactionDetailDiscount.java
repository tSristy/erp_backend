package org.enterprise.pos.retail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "pos_retail_transaction_detail_discounts")
@Getter
@Setter
public class RetailTransactionDetailDiscount extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_detail_id", nullable = false)
    private RetailTransactionDetail transactionDetail;

    private String discountName; // e.g., "Seasonal", "Loyalty", "Coupon"

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal discountAmount;

}
