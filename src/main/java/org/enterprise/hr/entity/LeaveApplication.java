package org.enterprise.hr.entity;

import java.time.LocalDate;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_leave_application")
@Getter
@Setter
public class LeaveApplication extends AuditableEntity {

    @ManyToOne private Employee employee;
    @ManyToOne private LeaveType leaveType;

    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer days;
    private String reason;
    private String status; // Pending, Approved, Rejected

}
