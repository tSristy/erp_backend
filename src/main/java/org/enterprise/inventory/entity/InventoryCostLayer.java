package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inv_cost_layers")
@Getter
@Setter
public class InventoryCostLayer extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    private String documentType; // e.g. "GRN", "PRODUCTION_RECEIVE"
    private Long documentId;

    private LocalDateTime receiptDate;

    @Column(precision = 18, scale = 6)
    private BigDecimal unitCost;

    @Column(precision = 18, scale = 6)
    private BigDecimal originalQty;

    @Column(precision = 18, scale = 6)
    private BigDecimal remainingQty;

}
