package org.enterprise.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import java.math.BigDecimal;

@Entity
@Table(name = "sal_sales_quotation_discounts")
@Getter
@Setter
public class SalesQuotationDiscount extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_quotation_id", nullable = false)
    private SalesQuotation salesQuotation;

    private String discountName;
    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;
}
