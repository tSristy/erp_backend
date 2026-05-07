package org.enterprise.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InventoryTransactionRequest {

    private Long itemId;

    private Long warehouseId;

    private Long locationId;

    private String transactionType;

    private String documentType;

    private Long documentId;

    private BigDecimal quantity;

    private BigDecimal unitCost;
}