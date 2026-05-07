package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "product_inventory")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductInventory extends AuditableEntity {

    private BigDecimal quantity = BigDecimal.ZERO;

    private BigDecimal reservedQty = BigDecimal.ZERO;

    private BigDecimal reorderLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;
}