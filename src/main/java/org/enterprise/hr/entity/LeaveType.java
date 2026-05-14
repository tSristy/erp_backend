package org.enterprise.hr.entity;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_leave_type")
@Getter
@Setter
public class LeaveType extends AuditableEntity {

    private String name;
    private String description;
    private Integer days;
    private String type; // Casual, Sick, Earned, etc

}
