package org.enterprise.hr.entity;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_employee_education")
@Getter
@Setter
public class EmployeeEducation extends AuditableEntity {

    @ManyToOne
    private Employee employee;

    private String degreeName;
    private String institute;

    private Integer passingYear;
}