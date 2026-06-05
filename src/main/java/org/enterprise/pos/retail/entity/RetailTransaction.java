package org.enterprise.pos.retail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.Warehouse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pos_retail_transactions")
@Getter
@Setter
public class RetailTransaction extends AuditableEntity {

    private String transactionNo;

    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private BusinessPartner customer; // Can be null for walk-in

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private TransactionType type = TransactionType.SALES;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_transaction_id")
    private RetailTransaction referenceTransaction;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RetailTransactionDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RetailTransactionPayment> payments = new ArrayList<>();

    public enum TransactionStatus {
        PENDING, COMPLETED, VOIDED
    }

    public enum TransactionType {
        SALES, RETURN
    }

    public void addDetail(RetailTransactionDetail detail) {
        details.add(detail);
        detail.setTransaction(this);
    }

    public void addPayment(RetailTransactionPayment payment) {
        payments.add(payment);
        payment.setTransaction(this);
    }
}
