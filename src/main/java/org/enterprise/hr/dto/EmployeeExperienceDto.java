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
public class EmployeeExperienceDto {

    private Long id;
    private Long employeeId;
    private String companyName;
    private String designation;
    private LocalDate fromDate;
    private LocalDate toDate;
}
