package org.enterprise.hr.entity;

import java.time.LocalDate;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_employee_roster")
@Getter
@Setter
public class EmployeeRoster extends AuditableEntity {

    @ManyToOne
    private Employee employee;

    private LocalDate dutyDate;

    @ManyToOne
    private Shift shift;

    private Boolean holiday;
    private Boolean weeklyOff;
}
