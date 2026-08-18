package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.sales.entity.SalesInvoice;

import java.math.BigDecimal;

@Entity
@Table(name = "fin_payment_receipt_details")
@Getter
@Setter
public class PaymentReceiptDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private PaymentReceipt receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_invoice_id")
    private SalesInvoice salesInvoice;

    @Column(precision = 18, scale = 2)
    private BigDecimal allocatedAmount = BigDecimal.ZERO;
}
