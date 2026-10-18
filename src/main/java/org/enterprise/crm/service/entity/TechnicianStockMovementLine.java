package org.enterprise.crm.service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "crm_technician_stock_movement_lines")
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TechnicianStockMovementLine extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movement_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "lines"})
    private TechnicianStockMovement movement;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "variant_label")
    private String variantLabel;

    @Column(precision = 18, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "unit_cost", precision = 18, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;
}
