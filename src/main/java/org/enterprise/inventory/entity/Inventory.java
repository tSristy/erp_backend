package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(
        name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "product_id", "location_id"})
        },
        indexes = {
                @Index(name = "idx_inv_product", columnList = "product_id"),
                @Index(name = "idx_inv_location", columnList = "location_id")
        }
)
@Getter
@Setter
@SQLDelete(sql = "UPDATE inventory SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Inventory extends AuditableEntity {

    private BigDecimal quantity = BigDecimal.ZERO;

    private BigDecimal reservedQty = BigDecimal.ZERO;

    private BigDecimal availableQty = BigDecimal.ZERO;

    private BigDecimal reorderLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;
}