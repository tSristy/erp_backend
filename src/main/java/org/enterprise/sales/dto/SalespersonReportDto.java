package org.enterprise.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalespersonReportDto {
    private Long salespersonId; // This represents createdBy user ID
    private Long invoiceCount;
    private BigDecimal totalRevenue;
}
