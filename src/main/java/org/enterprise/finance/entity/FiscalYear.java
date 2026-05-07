package org.enterprise.finance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDate;

@Entity
@Table(name = "fin_fiscal_years")
@Getter
@Setter
public class FiscalYear extends AuditableEntity {

    private String yearCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean active;

    private Boolean closed;
}
