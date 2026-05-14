package org.enterprise.hr.entity;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_payslip_component")
@Getter
@Setter
public class PayslipComponent extends AuditableEntity {

    @ManyToOne
    private Payslip payslip;

    @ManyToOne
    private SalaryComponent salaryComponent;

    private String type; // Earning or Deduction
    private Double amount;

}
