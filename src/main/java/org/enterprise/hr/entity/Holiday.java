package org.enterprise.hr.entity;

import java.time.LocalDate;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_holiday")
@Getter
@Setter
public class Holiday extends AuditableEntity {
    private String code;
    private String name;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String type; // National, Company, Religious, etc
}
