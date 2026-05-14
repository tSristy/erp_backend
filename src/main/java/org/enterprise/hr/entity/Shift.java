package org.enterprise.hr.entity;

import java.time.LocalTime;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_shift")
@Getter
@Setter
public class Shift extends AuditableEntity {
    private String code;
    private String name;

    private LocalTime inTime;
    private LocalTime outTime;

    private Integer graceMinutes;
}
