package org.enterprise.sales.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.enterprise.common.dto.AuditableDto;
import org.enterprise.sales.entity.SalesQuotation.QuotationStatus;

@Data
public class SalesQuotationDto extends AuditableDto {
    private String quotationNo;
    private LocalDate quotationDate;
    private LocalDate validUntil;
    private QuotationStatus status;
    private org.enterprise.sales.entity.SalesQuotation.SalesChannel salesChannel;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal subTotal;
    private BigDecimal discountTotal;
    private BigDecimal vatTotal;
    private BigDecimal totalAmount;
    private List<SalesQuotationDiscountDto> discounts;
    private List<SalesQuotationDetailDto> details;
}
