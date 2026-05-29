package org.enterprise.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePromotionDto {

    private Long id;
    private Long employeeId;
    private Long previousDesignationId;
    private Long newDesignationId;
    private LocalDate promotionDate;
    private String remarks;
}
