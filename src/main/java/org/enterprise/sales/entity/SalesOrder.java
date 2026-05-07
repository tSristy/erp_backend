package org.enterprise.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.Warehouse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "sal_sales_orders")
@Getter
@Setter
public class SalesOrder extends AuditableEntity {

    private String orderNo;

    private LocalDate orderDate;

    @Enumerated(EnumType.STRING)
    private SalesOrderStatus status = SalesOrderStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner customer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesOrderDetail> details;

    @Enumerated(EnumType.STRING)
    private OrderType orderType = OrderType.STANDARD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_order_id")
    private SalesOrder referenceOrder;

    public enum SalesOrderStatus {
        DRAFT, CONFIRMED, SHIPPED, INVOICED, CANCELLED
    }

    public enum OrderType {
        STANDARD, RETURN
    }
}
