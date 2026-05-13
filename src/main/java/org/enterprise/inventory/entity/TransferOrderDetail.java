package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_transfer_order_details")
@Getter
@Setter
public class TransferOrderDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private TransferOrder transferOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location sourceLocation; // Optional target location request

    @ManyToOne(fetch = FetchType.LAZY)
    private Location destinationLocation;

    @Column(precision = 18, scale = 6)
    private BigDecimal orderedQuantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 6)
    private BigDecimal transferredQuantity = BigDecimal.ZERO; // Quantity actually dispatched via StockTransfer
}
