package org.enterprise.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderRequest {

    private LocalDate poDate;

    private Long vendorId;

    private Long warehouseId;

    private List<PurchaseOrderDetailRequest> details;
}