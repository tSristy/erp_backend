package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.finance.enums.CostCenterType;
import org.enterprise.organization.entity.Branch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(
        name = "cost_centers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "code"})
        }
)
@Getter
@Setter
public class CostCenter extends AuditableEntity {

    private String code;
    private String name;
    private String shortName;

    // DIRECT, INDIRECT, ADMIN, SHARED, PRODUCTION
    @Enumerated(EnumType.STRING)
    private CostCenterType type;

    private String category;
    // HR, IT, SALES, FINANCE, LOGISTICS

    private String allocationMethod;
    // MANUAL, HEADCOUNT, REVENUE, AREA

    private Boolean budgetControlled = false;

    private BigDecimal monthlyBudget;
    private BigDecimal yearlyBudget;

    private Boolean active = true;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProfitCenter profitCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    private CostCenter parent;

    @OneToMany(mappedBy = "parent")
    private List<CostCenter> children;
}

