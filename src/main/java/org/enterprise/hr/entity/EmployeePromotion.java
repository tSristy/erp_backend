package org.enterprise.hr.entity;

import java.time.LocalDate;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_employee_promotion")
@Getter
@Setter
public class EmployeePromotion extends AuditableEntity {

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private Designation previousDesignation;

    @ManyToOne
    private Designation newDesignation;

    private LocalDate promotionDate;

    private String remarks;

}
