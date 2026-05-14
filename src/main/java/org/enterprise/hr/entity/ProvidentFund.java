package org.enterprise.hr.entity;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_provident_fund")
@Getter
@Setter
public class ProvidentFund extends AuditableEntity {

    @ManyToOne
    private Employee employee;

    private Double employeeContribution;
    private Double employerContribution;

}
