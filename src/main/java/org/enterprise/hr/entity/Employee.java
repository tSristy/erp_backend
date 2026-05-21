package org.enterprise.hr.entity;

import java.time.LocalDate;

import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.organization.entity.Branch;
import org.enterprise.organization.entity.Company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_employee")
@Getter
@Setter
public class Employee extends AuditableEntity {
    @Column(unique = true)
    private String employeeCode;

    private String firstName;
    private String lastName;

    private String fullName;

    private String gender;
    private String maritalStatus;

    private LocalDate dateOfBirth;
    private LocalDate joiningDate;

    private String mobile;
    private String email;

    private String presentAddress;
    private String permanentAddress;

//    @ManyToOne
//    private Company company;

    @ManyToOne
    private Branch branch;

    @ManyToOne
    private Department department;

    @ManyToOne
    private Designation designation;

    @ManyToOne
    private Shift defaultShift;

    private Boolean active;
}
