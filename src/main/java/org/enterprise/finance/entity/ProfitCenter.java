package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.organization.entity.Branch;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(
        name = "profit_centers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "code"})
        }
)
@Getter
@Setter
public class ProfitCenter extends AuditableEntity {

    private String code;
    private String name;
    private String shortName;

    private String type; // SALES, FACTORY, PROJECT, REGION

    private String managerName;
    private String managerEmail;
    private String contactNo;

    private String currencyCode;

    private Boolean active = true;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProfitCenter parent;

    @OneToMany(mappedBy = "parent")
    private List<ProfitCenter> children;
}
