package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "fin_payment_vouchers")
@Getter
@Setter
public class PaymentVoucher extends AuditableEntity {

    @Column(unique = true)
    private String voucherNo;

    private LocalDate voucherDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    private BankAccount bankAccount;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount;

    private String paymentMethod;
    
    private String referenceNo;

    private Long workflowInstanceId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.DRAFT;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentVoucherDetail> details;

    public enum PaymentStatus {
        DRAFT, POSTED, CANCELLED
    }
}
