package org.enterprise.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionYieldReportDto {
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal totalPlannedQuantity;
    private BigDecimal totalProducedQuantity;
    private BigDecimal yieldPercentage;
}
