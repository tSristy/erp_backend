package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.PurchaseInvoice;

import java.math.BigDecimal;

@Entity
@Table(name = "fin_payment_voucher_details")
@Getter
@Setter
public class PaymentVoucherDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private PaymentVoucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id")
    private PurchaseInvoice purchaseInvoice;

    @Column(precision = 18, scale = 2)
    private BigDecimal allocatedAmount = BigDecimal.ZERO;
}
