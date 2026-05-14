package org.enterprise.hr.entity;

import java.time.LocalDate;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_payroll_process")
@Getter
@Setter
public class PayrollProcess extends AuditableEntity {

    private LocalDate processDate;

    private Integer processMonth; // 1 to 12
    private Integer processYear;

    private String status; // Draft, Approved, Paid

    private Double totalEarning;
    private Double totalDeduction;
    private Double netPayment;

}
