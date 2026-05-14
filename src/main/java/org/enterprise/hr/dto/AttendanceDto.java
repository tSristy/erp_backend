package org.enterprise.hr.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AttendanceDto {
    private Long employeeId;

    private LocalDate attendanceDate;

    private LocalDateTime inTime;
    private LocalDateTime outTime;
}
