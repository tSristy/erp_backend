package org.enterprise.sales.dto;

import lombok.Data;
import java.math.BigDecimal;
import org.enterprise.common.dto.AuditableDto;

@Data
public class SalesOrderDetailDto extends AuditableDto {
    private Long salesOrderId;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private BigDecimal discountTotal;
    private java.util.List<SalesOrderDetailDiscountDto> discounts;
    private BigDecimal shippedQuantity;
    private BigDecimal returnedQuantity;
}
