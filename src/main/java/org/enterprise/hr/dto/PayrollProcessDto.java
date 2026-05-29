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
public class PayrollProcessDto {

    private Long id;
    private LocalDate processDate;
    private Integer processMonth;
    private Integer processYear;
    private String status;
    private Double totalEarning;
    private Double totalDeduction;
    private Double netPayment;
}
