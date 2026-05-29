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
public class LeaveApplicationDto {

    private Long id;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer days;
    private String reason;
    private String status;
}
