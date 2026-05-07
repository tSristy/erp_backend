package org.enterprise.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseOrderDetailCostRequest {

    private Long costHeadId;

    private BigDecimal amount;

    private Boolean includedInInventoryCost;
}