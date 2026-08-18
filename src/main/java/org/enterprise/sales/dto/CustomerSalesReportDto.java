package org.enterprise.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSalesReportDto {
    private Long customerId;
    private String customerName;
    private Long invoiceCount;
    private BigDecimal totalRevenue;
}
