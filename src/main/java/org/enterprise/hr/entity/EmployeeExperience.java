package org.enterprise.hr.entity;

import java.time.LocalDate;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_employee_experience")
@Getter
@Setter
public class EmployeeExperience extends AuditableEntity {

    @ManyToOne
    private Employee employee;

    private String companyName;
    private String designation;

    private LocalDate fromDate;
    private LocalDate toDate;
}
