package org.enterprise.hr.entity;

import java.time.LocalDateTime;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_mobile_attendance")
@Getter
@Setter
public class MobileAttendance extends AuditableEntity {

    @ManyToOne
    private Employee employee;

    private LocalDateTime attendanceTime;

    private Double latitude;
    private Double longitude;

    private String attendanceType;

}
