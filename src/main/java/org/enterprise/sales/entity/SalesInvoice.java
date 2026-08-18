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
@Table(name = "sal_sales_invoices")
@Getter
@Setter
public class SalesInvoice extends AuditableEntity {

    private String invoiceNo;

    private LocalDate invoiceDate;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    private DeliveryNote deliveryNote;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner customer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @Column(precision = 18, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "salesInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesInvoiceDiscount> discounts;

    @OneToMany(mappedBy = "salesInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesInvoiceDetail> details;

    @Enumerated(EnumType.STRING)
    private InvoiceType invoiceType = InvoiceType.INVOICE;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.hr.entity.Employee salesPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.hr.entity.Employee territoryManager;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.hr.entity.Employee areaManager;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.hr.entity.Employee regionalManager;

    public enum InvoiceStatus {
        DRAFT, POSTED, PAID, CANCELLED
    }

    public enum InvoiceType {
        INVOICE, CREDIT_MEMO
    }

    public enum SalesChannel {
        B2B, B2C, RETAIL, ECOMMERCE
    }

    @Enumerated(EnumType.STRING)
    private SalesChannel salesChannel = SalesChannel.B2C;
}
