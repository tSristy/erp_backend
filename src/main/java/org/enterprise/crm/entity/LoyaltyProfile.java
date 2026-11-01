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

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BusinessPartner customer;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal totalPointsEarned = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal currentPointsBalance = BigDecimal.ZERO;

    private String loyaltyTier = "BRONZE"; // e.g., BRONZE, SILVER, GOLD

    @com.fasterxml.jackson.annotation.JsonGetter("customerId")
    public Long resolveCustomerId() {
        return customer != null ? customer.getId() : null;
    }

    @com.fasterxml.jackson.annotation.JsonSetter("customerId")
    public void assignCustomerId(Long customerId) {
        if (customerId != null) {
            if (this.customer == null) {
                this.customer = new BusinessPartner();
            }
            this.customer.setId(customerId);
        }
    }

    @com.fasterxml.jackson.annotation.JsonGetter("customerName")
    public String resolveCustomerName() {
        if (customer == null) return null;
        try {
            return customer.getName();
        } catch (Exception e) {
            return null;
        }
    }
}
