package org.enterprise.pos.retail.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import org.enterprise.common.dto.AuditableDto;

@Data
public class RetailTransactionDetailDto extends AuditableDto {
    private Long transactionId;
    private Long productId;
    private Long variantId;
    private String variantSku;
    private String serialNo;
    private List<String> serialNumbers;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
    private List<RetailTransactionDetailDiscountDto> discounts;
}
