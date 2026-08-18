package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.organization.entity.Branch;

import java.math.BigDecimal;

@Entity
@Table(name = "fin_dimension_balances",
        indexes = {
                @Index(name = "idx_dimbal_account", columnList = "account_id"),
                @Index(name = "idx_dimbal_type_code", columnList = "dimensionType, dimensionCode"),
                @Index(name = "idx_dimbal_period", columnList = "fiscal_period_id"),
                @Index(name = "idx_dimbal_branch", columnList = "branch_id")
        })
@Getter
@Setter
public class DimensionBalance extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    private FiscalYear fiscalYear;

    @ManyToOne(fetch = FetchType.LAZY)
    private FiscalPeriod fiscalPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    private Branch branch;

    @Column(length = 50)
    private String dimensionType;

    @Column(length = 50)
    private String dimensionCode;

    @Column(precision = 18, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal periodDebit = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal periodCredit = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal closingBalance = BigDecimal.ZERO;
}
