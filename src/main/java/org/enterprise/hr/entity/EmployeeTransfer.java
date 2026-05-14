package org.enterprise.hr.entity;

import java.time.LocalDate;

import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.organization.entity.Branch;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_employee_transfer")
@Getter
@Setter
public class EmployeeTransfer extends AuditableEntity {

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private Branch previousBranch;

    @ManyToOne
    private Branch newBranch;

    @ManyToOne
    private Department previousDepartment;

    @ManyToOne
    private Department newDepartment;

    private LocalDate transferDate;

    private String remarks;

}
