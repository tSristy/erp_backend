package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.organization.entity.Branch;

import java.math.BigDecimal;

@Entity
@Table(name = "fin_account_balances",
        indexes = {
                @Index(name = "idx_ab_account", columnList = "account_id"),
                @Index(name = "idx_ab_period", columnList = "fiscal_period_id"),
                @Index(name = "idx_ab_branch", columnList = "branch_id")
        })
@Getter
@Setter
public class AccountBalance extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    private FiscalYear fiscalYear;

    @ManyToOne(fetch = FetchType.LAZY)
    private FiscalPeriod fiscalPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    private Branch branch;

    @Column(precision = 18, scale = 2)
    private BigDecimal openingDebit = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal openingCredit = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal periodDebit = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal periodCredit = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal closingDebit = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal closingCredit = BigDecimal.ZERO;
}