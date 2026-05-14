package org.enterprise.hr.entity;

import java.time.LocalDate;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_employee_increment")
@Getter
@Setter
public class EmployeeIncrement extends AuditableEntity {

    @ManyToOne
    private Employee employee;

    private Double previousSalary;

    private Double newSalary;

    private LocalDate incrementDate;

    private String remarks;

}
