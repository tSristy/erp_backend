package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "crm_service_estimate_details")
@Getter
@Setter
public class ServiceEstimateDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_estimate_id")
    private ServiceEstimate serviceEstimate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // For parts. If null, it's labor.

    private String description;

    @Column(precision = 18, scale = 2)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    private Boolean isPart = true; // true if part, false if labor
}
