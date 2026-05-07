package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "pur_purchase_order_details")
@Getter
@Setter
public class PurchaseOrderDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private BigDecimal orderedQty = BigDecimal.ZERO;

    private BigDecimal receivedQty = BigDecimal.ZERO;

    private BigDecimal returnedQty = BigDecimal.ZERO;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "purchaseOrderDetail",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PurchaseOrderDetailCost> costs;
}