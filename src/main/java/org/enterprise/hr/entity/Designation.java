package org.enterprise.hr.entity;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_designation")
@Getter
@Setter
public class Designation extends AuditableEntity {

    private String code;

    private String name;

    private String grade;
}