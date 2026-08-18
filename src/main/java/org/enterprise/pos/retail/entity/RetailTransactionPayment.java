package org.enterprise.pos.retail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

import org.enterprise.common.event.PosPayment;

@Entity
@Table(name = "pos_retail_transaction_payments")
@Getter
@Setter
public class RetailTransactionPayment extends AuditableEntity implements PosPayment {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"details", "payments"})
    private RetailTransaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode; // CASH, CARD, MFS, DUE

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(length = 50)
    private String referenceNumber;

    @Override
    public String getPaymentModeName() {
        return paymentMode != null ? paymentMode.name() : null;
    }

    public enum PaymentMode {
        CASH, CARD, MFS, DUE, LOYALTY_POINTS
    }
}
