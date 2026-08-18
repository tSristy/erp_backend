package org.enterprise.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseSalesReportDto {
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long invoiceCount;
    private BigDecimal totalRevenue;
}
