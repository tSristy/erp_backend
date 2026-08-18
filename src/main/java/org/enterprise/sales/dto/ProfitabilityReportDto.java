package org.enterprise.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfitabilityReportDto {
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal totalQuantity;
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal grossProfit; // Revenue - Cost
}
