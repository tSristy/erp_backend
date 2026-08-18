package org.enterprise.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesReportDto {
    private LocalDate invoiceDate;
    private Long invoiceCount;
    private BigDecimal totalRevenue;
    private BigDecimal totalDiscounts;
}
