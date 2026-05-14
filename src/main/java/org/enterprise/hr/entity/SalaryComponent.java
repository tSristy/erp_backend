package org.enterprise.hr.entity;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_salary_component")
@Getter
@Setter
public class SalaryComponent extends AuditableEntity {

    private String name;
    private String description;
    private String type; // Earning or Deduction
    private String calculationMethod; // Fixed or Percentage
    private Double amount;

}
