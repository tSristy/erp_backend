package org.enterprise.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileAttendanceDto {

    private Long id;
    private Long employeeId;
    private LocalDateTime attendanceTime;
    private Double latitude;
    private Double longitude;
    private String attendanceType;
}
