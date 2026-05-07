package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_item_cost_layers")
@Getter
@Setter
public class ItemCostLayer extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    private String sourceDocumentType;

    private Long sourceDocumentId;

    private BigDecimal originalQty;

    private BigDecimal remainingQty;

    private BigDecimal unitCost;

    private Boolean closed = false;
}