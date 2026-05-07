package org.enterprise.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InventoryLedgerResponse {

    private Long id;

    private String transactionType;

    private String documentType;

    private Long documentId;

    private LocalDateTime transactionDate;

    private BigDecimal qtyIn;

    private BigDecimal qtyOut;

    private BigDecimal balanceQty;

    private BigDecimal unitCost;

    private BigDecimal totalCost;
}