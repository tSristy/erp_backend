package org.enterprise.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "sal_delivery_note_details")
@Getter
@Setter
public class DeliveryNoteDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private DeliveryNote deliveryNote;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesOrderDetail salesOrderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Column(precision = 18, scale = 6)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO; // Track COGS at time of delivery
}
