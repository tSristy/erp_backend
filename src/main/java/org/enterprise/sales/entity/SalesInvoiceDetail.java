package org.enterprise.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "sal_sales_invoice_details")
@Getter
@Setter
public class SalesInvoiceDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesInvoice salesInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    private DeliveryNoteDetail deliveryNoteDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Column(precision = 18, scale = 6)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;
}
