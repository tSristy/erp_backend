package org.enterprise.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyProductionReportDto {
    private LocalDate orderDate;
    private Long orderCount;
    private BigDecimal totalPlannedQuantity;
    private BigDecimal totalProducedQuantity;
}
