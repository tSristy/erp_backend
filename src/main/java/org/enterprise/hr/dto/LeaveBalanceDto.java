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
public class LeaveBalanceDto {

    private Long id;
    private Long employeeId;
    private Long leaveTypeId;
    private Integer totalDays;
    private Integer usedDays;
    private Integer remainingDays;
    private LocalDate balanceDate;
}
