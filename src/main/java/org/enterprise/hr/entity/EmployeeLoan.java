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
    private String status;

    @jakarta.persistence.OneToMany(mappedBy = "loan", cascade = jakarta.persistence.CascadeType.ALL, fetch = jakarta.persistence.FetchType.LAZY)
    private java.util.List<LoanInstallment> installments;

}
