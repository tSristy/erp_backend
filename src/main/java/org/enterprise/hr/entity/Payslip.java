package org.enterprise.hr.entity;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_payslip")
@Getter
@Setter
public class Payslip extends AuditableEntity {

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private PayrollProcess payrollProcess;

    private Integer processMonth;
    private Integer processYear;

    private Double grossSalary;
    
    private Double totalEarning;
    private Double totalDeduction;
    private Double netPayable;

    private String status; // Draft, Generated, Paid

}
