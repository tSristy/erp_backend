package org.enterprise.crm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;

import java.math.BigDecimal;

@Entity
@Table(name = "crm_loyalty_profiles")
@Getter
@Setter
public class LoyaltyProfile extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private BusinessPartner customer;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal totalPointsEarned = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal currentPointsBalance = BigDecimal.ZERO;

    private String loyaltyTier = "BRONZE"; // e.g., BRONZE, SILVER, GOLD
}
