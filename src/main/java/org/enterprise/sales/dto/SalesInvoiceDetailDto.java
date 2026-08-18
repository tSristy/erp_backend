package org.enterprise.sales.dto;

import lombok.Data;
import java.math.BigDecimal;
import org.enterprise.common.dto.AuditableDto;

@Data
public class SalesInvoiceDetailDto extends AuditableDto {
    private Long salesInvoiceId;
    private Long deliveryNoteDetailId;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private BigDecimal discountTotal;
    private java.util.List<SalesInvoiceDetailDiscountDto> discounts;
}
