package org.enterprise.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import java.math.BigDecimal;

@Entity
@Table(name = "sal_sales_order_detail_discounts")
@Getter
@Setter
public class SalesOrderDetailDiscount extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_detail_id", nullable = false)
    private SalesOrderDetail salesOrderDetail;

    private String discountName;
    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;
}
