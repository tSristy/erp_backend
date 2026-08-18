package org.enterprise.finance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "fin_loans")
@Getter
@Setter
public class Loan extends AuditableEntity {
    private String code;
    private String name;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Boolean active = true;

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    private Account principalAccount;

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    private Account interestAccount;
}
