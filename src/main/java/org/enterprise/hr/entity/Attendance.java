package org.enterprise.hr.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_attendance")
@Getter
@Setter
public class Attendance {

    @ManyToOne
    private Employee employee;

    private LocalDate attendanceDate;

    private LocalDateTime inTime;
    private LocalDateTime outTime;

    @ManyToOne
    private Shift shift;

    private Boolean late;
    private Boolean earlyOut;
    private Boolean absent;

    private BigDecimal workedHours;
    private BigDecimal overtimeHours;

}