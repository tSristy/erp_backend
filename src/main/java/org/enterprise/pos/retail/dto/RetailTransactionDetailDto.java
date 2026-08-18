package org.enterprise.pos.retail.dto;

import lombok.Data;
import java.math.BigDecimal;
import org.enterprise.common.dto.AuditableDto;

@Data
public class RetailTransactionDetailDto extends AuditableDto {
    private Long transactionId;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
}
