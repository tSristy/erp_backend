package org.enterprise.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeIncrementDto {

    private Long id;
    private Long employeeId;
    private Double previousSalary;
    private Double newSalary;
    private LocalDate incrementDate;
    private String remarks;
}
