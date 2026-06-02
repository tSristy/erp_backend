package org.enterprise.pos.retail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "pos_retail_transaction_payments")
@Getter
@Setter
public class RetailTransactionPayment extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private RetailTransaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode; // CASH, CARD, MFS, DUE

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    private String referenceNumber; // e.g., Transaction ID for MFS or Card

    public enum PaymentMode {
        CASH, CARD, MFS, DUE, LOYALTY_POINTS
    }
}
