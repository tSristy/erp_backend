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
@Table(name = "fin_payment_receipts")
@Getter
@Setter
public class PaymentReceipt extends AuditableEntity {

    @Column(unique = true)
    private String receiptNo;

    private LocalDate receiptDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner customer;

    @ManyToOne(fetch = FetchType.LAZY)
    private BankAccount bankAccount;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount;

    private String paymentMethod;
    
    private String referenceNo; // e.g. check number

    private Long workflowInstanceId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.DRAFT;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentReceiptDetail> details;

    public enum PaymentStatus {
        DRAFT, POSTED, CANCELLED
    }
}
