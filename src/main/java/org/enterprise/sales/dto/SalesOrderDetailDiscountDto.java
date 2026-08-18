package org.enterprise.sales.dto;

import lombok.Data;
import org.enterprise.common.dto.AuditableDto;
import java.math.BigDecimal;

@Data
public class SalesOrderDetailDiscountDto extends AuditableDto {
    private String discountName;
    private BigDecimal discountAmount;
}
