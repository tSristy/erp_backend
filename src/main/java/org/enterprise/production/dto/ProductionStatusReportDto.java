package org.enterprise.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.enterprise.production.entity.ManufacturingOrder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionStatusReportDto {
    private ManufacturingOrder.OrderStatus status;
    private Long orderCount;
    private BigDecimal totalPlannedQuantity;
    private BigDecimal totalProducedQuantity;
}
