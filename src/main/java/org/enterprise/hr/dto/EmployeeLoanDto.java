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
public class EmployeeLoanDto {

    private Long id;
    private Long employeeId;
    private Double amount;
    private Double installment;
    private Integer months;
    private Integer paidMonths;
}
