package org.enterprise.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductProductionReportDto {
    private Long productId;
    private String productCode;
    private String productName;
    private Long orderCount;
    private BigDecimal totalPlannedQuantity;
    private BigDecimal totalProducedQuantity;
}
