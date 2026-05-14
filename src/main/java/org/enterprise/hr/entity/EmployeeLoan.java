package org.enterprise.hr.entity;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_employee_loan")
@Getter
@Setter
public class EmployeeLoan extends AuditableEntity {

    @ManyToOne
    private Employee employee;

    private Double amount;
    private Double installment;
    private Integer months;
    private Integer paidMonths;

}
