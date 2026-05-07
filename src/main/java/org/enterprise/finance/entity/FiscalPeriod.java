package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.finance.enums.PeriodStatus;

import java.time.LocalDate;

@Entity
@Table(name = "fin_fiscal_periods")
@Getter
@Setter
public class FiscalPeriod extends AuditableEntity {

    private String periodName;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private PeriodStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private FiscalYear fiscalYear;
}
