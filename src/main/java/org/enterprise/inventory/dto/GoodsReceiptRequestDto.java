package org.enterprise.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class GoodsReceiptRequestDto {

    private LocalDate grnDate;

    private Long vendorId;

    private Long warehouseId;

    private Long purchaseOrderId;

    private List<GoodsReceiptLineDto> details;
}
