package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.finance.enums.AccountType;

@Entity
@Table(name = "fin_accounts")
@Getter
@Setter
public class Account extends AuditableEntity {

    private String code;

    private String name;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account parent;

    private Boolean allowPosting = true;

    private Boolean active = true;

    // Dimension Requirements Configuration
    @Column(name = "is_bp_required")
    private boolean isBusinessPartnerRequired = false;

    @Column(name = "is_cost_center_required")
    private boolean isCostCenterRequired = false;

    @Column(name = "is_project_required")
    private boolean isProjectRequired = false;

    @Column(name = "is_lc_required")
    private boolean isLcRequired = false;

    @Column(name = "is_loan_required")
    private boolean isLoanRequired = false;
}
