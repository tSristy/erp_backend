package org.enterprise.hr.entity;

import java.time.LocalDate;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_leave_balance")
@Getter
@Setter
public class LeaveBalance extends AuditableEntity {

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private LeaveType leaveType;

    private Integer totalDays;
    private Integer usedDays;
    private Integer remainingDays;

    private LocalDate balanceDate;

}
