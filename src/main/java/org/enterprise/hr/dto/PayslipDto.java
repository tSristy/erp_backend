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
public class PayslipDto {

    private Long id;
    private Long employeeId;
    private Long payrollProcessId;
    private Integer processMonth;
    private Integer processYear;
    private Double grossSalary;
    private Double totalEarning;
    private Double totalDeduction;
    private Double netPayable;
    private String status;
}
