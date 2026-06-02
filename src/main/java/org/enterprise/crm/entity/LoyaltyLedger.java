package org.enterprise.crm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_loyalty_ledgers")
@Getter
@Setter
public class LoyaltyLedger extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private LoyaltyProfile profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType; // EARNED or REDEEMED

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal points;

    private String referenceTransactionNo; // POS Transaction No

    private LocalDateTime transactionDate;

    public enum TransactionType {
        EARNED, REDEEMED
    }
}
