package org.enterprise.production.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.Warehouse;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "prd_manufacturing_orders")
@Getter
@Setter
public class ManufacturingOrder extends AuditableEntity {

    private String orderNo;

    private LocalDate orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product finishedGood;

    @ManyToOne(fetch = FetchType.LAZY)
    private BillOfMaterial bom;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse productionWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    private Routing routing;

    @Column(precision = 18, scale = 6)
    private BigDecimal plannedQuantity;

    @Column(precision = 18, scale = 6)
    private BigDecimal producedQuantity = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PLANNED;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.inventory.entity.Batch batch;

    @ElementCollection
    @CollectionTable(name = "prd_manufacturing_order_serials", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "serial_no")
    private java.util.List<String> serialNumbers = new java.util.ArrayList<>();

    public enum OrderStatus {
        PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
