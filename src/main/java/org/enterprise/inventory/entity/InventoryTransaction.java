package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(
        name = "inventory_transactions",
        indexes = {
                @Index(name = "idx_tx_product", columnList = "product_id"),
                @Index(name = "idx_tx_location", columnList = "location_id")
        }
)
@Getter
@Setter
public class InventoryTransaction extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Double quantity;

    private String referenceNo;

    private String referenceType;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    public enum TransactionType {
        IN, OUT, TRANSFER, ADJUSTMENT
    }
}