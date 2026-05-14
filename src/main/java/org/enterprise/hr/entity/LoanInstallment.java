package org.enterprise.hr.entity;

import java.time.LocalDate;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_loan_installment")
@Getter
@Setter
public class LoanInstallment extends AuditableEntity {

    @ManyToOne
    private EmployeeLoan loan;

    private LocalDate dueDate;

    private Double amount;

}
