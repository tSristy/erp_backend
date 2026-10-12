package org.enterprise.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalespersonReportDto {
    private Long salespersonId; // Represents assigned employee ID or createdBy user ID
    private String salespersonName;
    private Long invoiceCount;
    private BigDecimal totalRevenue;

    public SalespersonReportDto(Long salespersonId, Long invoiceCount, BigDecimal totalRevenue) {
        this.salespersonId = salespersonId;
        this.salespersonName = null;
        this.invoiceCount = invoiceCount;
        this.totalRevenue = totalRevenue;
    }
}
