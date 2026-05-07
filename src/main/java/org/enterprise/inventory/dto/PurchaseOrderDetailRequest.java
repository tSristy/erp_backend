package org.enterprise.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderDetailRequest {

    private Long itemId;

    private BigDecimal qty;

    private BigDecimal unitPrice;

    private List<PurchaseOrderDetailCostRequest> costs;
}