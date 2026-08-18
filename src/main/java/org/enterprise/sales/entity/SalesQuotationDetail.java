package org.enterprise.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "sal_sales_quotation_details")
@Getter
@Setter
public class SalesQuotationDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesQuotation salesQuotation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Column(precision = 18, scale = 6)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 6)
    private BigDecimal orderedQuantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @OneToMany(mappedBy = "salesQuotationDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<SalesQuotationDetailDiscount> discounts;
}
