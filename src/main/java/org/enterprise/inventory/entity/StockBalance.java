package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_stock_balances",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "item_id",
                                "warehouse_id",
                                "location_id",
                                "company_id",
                                "batch_id"
                        }
                )
        })
@Getter
@Setter
public class StockBalance extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    private Batch batch;

    private BigDecimal quantity = BigDecimal.ZERO;

    private BigDecimal averageCost = BigDecimal.ZERO;

    private BigDecimal totalValue = BigDecimal.ZERO;
}