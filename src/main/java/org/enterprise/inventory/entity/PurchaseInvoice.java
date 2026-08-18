package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "pur_purchase_invoices")
@Getter
@Setter
public class PurchaseInvoice extends AuditableEntity {

    @Column(unique = true)
    private String invoiceNo;

    private LocalDate invoiceDate;
    
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner vendor;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "purchaseInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseInvoiceDetail> details;

    @Enumerated(EnumType.STRING)
    private InvoiceType invoiceType = InvoiceType.INVOICE;

    public enum InvoiceStatus {
        DRAFT, POSTED, PAID, CANCELLED
    }

    public enum InvoiceType {
        INVOICE, DEBIT_MEMO
    }
}
