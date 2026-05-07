package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "pur_purchase_orders")
@Getter
@Setter
public class PurchaseOrder extends AuditableEntity {

    @Column(unique = true)
    private String poNo;

    private LocalDate poDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    private Long workflowInstanceId;

    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus status;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal discountAmount;

    private String remarks;

    private LocalDate expectedDeliveryDate;

    @OneToMany(mappedBy = "purchaseOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PurchaseOrderDetail> details;

    @Enumerated(EnumType.STRING)
    private OrderType orderType = OrderType.STANDARD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_order_id")
    private PurchaseOrder referenceOrder;

    public enum OrderType {
        STANDARD, RETURN
    }
}