package org.enterprise.hr.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AttendanceDto {
    private Long id;
    private Long employeeId;

    private LocalDate attendanceDate;

    private LocalDateTime inTime;
    private LocalDateTime outTime;
    
    private Long shiftId;
    
    private Boolean late;
    private Boolean earlyOut;
    private Boolean absent;
    
    private BigDecimal workedHours;
    private BigDecimal overtimeHours;
}
