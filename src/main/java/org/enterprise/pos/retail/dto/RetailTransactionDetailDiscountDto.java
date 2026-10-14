package org.enterprise.pos.retail.dto;

import lombok.Data;
import java.math.BigDecimal;
import org.enterprise.common.dto.AuditableDto;

@Data
public class RetailTransactionDetailDiscountDto extends AuditableDto {
    private Long id;
    private Long transactionDetailId;
    private String discountName;
    private BigDecimal discountAmount;
}
