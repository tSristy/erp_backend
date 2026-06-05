package org.enterprise.pos.restaurant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

import org.enterprise.common.event.PosPayment;

@Entity
@Table(name = "pos_restaurant_payments")
@Getter
@Setter
public class RestaurantPayment extends AuditableEntity implements PosPayment {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private RestaurantOrder order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode; // CASH, CARD, MFS, DUE

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(precision = 18, scale = 2)
    private BigDecimal tipAmount = BigDecimal.ZERO;

    @Column(length = 50)
    private String referenceNumber; // e.g., Card or MFS transaction ID

    @Override
    public String getPaymentModeName() {
        return paymentMode != null ? paymentMode.name() : null;
    }

    public enum PaymentMode {
        CASH, CARD, MFS, DUE, LOYALTY_POINTS
    }
}
