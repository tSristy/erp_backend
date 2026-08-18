package org.enterprise.sales.dto;

import lombok.Data;
import java.math.BigDecimal;
import org.enterprise.common.dto.AuditableDto;

@Data
public class SalesQuotationDetailDto extends AuditableDto {
    private Long salesQuotationId;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private BigDecimal discountTotal;
    private java.util.List<SalesQuotationDetailDiscountDto> discounts;
}
