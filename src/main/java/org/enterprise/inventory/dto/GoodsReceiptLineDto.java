package org.enterprise.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GoodsReceiptLineDto {

    private Long productId;

    private BigDecimal quantity;

    private BigDecimal unitCost;

    private String batchNo;

    private java.time.LocalDate manufactureDate;

    private java.time.LocalDate expiryDate;

    private java.util.List<String> serialNumbers;
}
