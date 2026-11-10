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
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "details"})
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "details"})
    private GoodsReceipt goodsReceipt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pur_purchase_invoice_goods_receipts",
            joinColumns = @JoinColumn(name = "purchase_invoice_id"),
            inverseJoinColumns = @JoinColumn(name = "goods_receipt_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "details"})
    private List<GoodsReceipt> goodsReceipts = new java.util.ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
