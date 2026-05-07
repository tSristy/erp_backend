package org.enterprise.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StockBalanceResponse {

    private Long itemId;

    private Long warehouseId;

    private Long locationId;

    private BigDecimal quantity;

    private BigDecimal averageCost;

    private BigDecimal totalValue;
}
