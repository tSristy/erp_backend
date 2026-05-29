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
public class EmployeeConfirmationDto {

    private Long id;
    private Long employeeId;
    private LocalDate confirmationDate;
    private String status;
    private String remarks;
}
