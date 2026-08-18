package org.enterprise.inventory.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderRequest {

    private LocalDate poDate;

    private Long vendorId;

    private Long warehouseId;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount;

    private String remarks;

    private LocalDate expectedDeliveryDate;

    private String currency;

    private BigDecimal exchangeRate;

    private String orderType;

    private Long letterOfCreditId;

    private List<PurchaseOrderDetailRequest> details;
}