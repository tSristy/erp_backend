package org.enterprise.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTransferDto {

    private Long id;
    private Long employeeId;
    private Long previousBranchId;
    private Long newBranchId;
    private Long previousDepartmentId;
    private Long newDepartmentId;
    private LocalDate transferDate;
    private String remarks;
}
